package com.lzy.mall.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.lzy.mall.bo.AdminUserDetails;
import com.lzy.mall.common.exception.Asserts;
import com.lzy.mall.common.util.RequestUtil;
import com.lzy.mall.dao.UmsAdminRoleRelationDao;
import com.lzy.mall.dto.UmsAdminParam;
import com.lzy.mall.dto.UpdateAdminPasswordParam;
import com.lzy.mall.mapper.UmsAdminLoginLogMapper;
import com.lzy.mall.mapper.UmsAdminMapper;
import com.lzy.mall.mapper.UmsAdminRoleRelationMapper;
import com.lzy.mall.model.*;
import com.lzy.mall.security.utils.JwtTokenUtil;
import com.lzy.mall.security.utils.SpringUtil;
import com.lzy.mall.service.UmsAdminCacheService;
import com.lzy.mall.service.UmsAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 后台用户管理Service实现类
 */
@Service
public class UmsAdminServiceImpl implements UmsAdminService , UserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsAdminServiceImpl.class);
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UmsAdminMapper adminMapper;
    @Autowired
    private UmsAdminRoleRelationMapper adminRoleRelationMapper;
    @Autowired
    private UmsAdminRoleRelationDao adminRoleRelationDao;
    @Autowired
    private UmsAdminLoginLogMapper loginLogMapper;

    /**
     * 根据用户名获取后台管理员
     * @param username 用户名
     * @return 后台管理员对象，如果不存在则返回null
     */
    @Override
    public UmsAdmin getAdminByUsername(String username) {
        //先从缓存中获取数据
        UmsAdmin admin = getCacheService().getAdmin(username);
        //如果缓存中存在该用户，直接返回缓存数据
        if (admin != null) return admin;
        //缓存中没有从数据库中获取
        UmsAdminExample example = new UmsAdminExample();
        //创建查询条件：用户名等于传入的username
        example.createCriteria().andUsernameEqualTo(username);
        //执行查询操作
        List<UmsAdmin> adminList = adminMapper.selectByExample(example);
        //判断查询结果是否存在
        if (adminList != null && adminList.size() > 0) {
            //获取查询结果的第一条数据
            admin = adminList.get(0);
            //将数据库中的数据存入缓存中，提高下次查询效率
            getCacheService().setAdmin(admin);
            //返回查询到的管理员对象
            return admin;
        }
        //如果数据库中也没有查询到，则返回null
        return null;
    }

    /**
     * 注册后台管理员账户
     *
     * @param umsAdminParam 用户注册参数，包含用户名、密码等信息
     * @return 注册成功返回用户对象，如果用户名已存在则返回null
     */
    @Override
    public UmsAdmin register(UmsAdminParam umsAdminParam) {
        // 创建新的管理员对象
        UmsAdmin umsAdmin = new UmsAdmin();
        // 将注册参数复制到管理员对象中
        BeanUtils.copyProperties(umsAdminParam, umsAdmin);
        // 设置创建时间为当前时间
        umsAdmin.setCreateTime(new Date());
        // 设置账号状态为启用（1表示启用）
        umsAdmin.setStatus(1);
        // 查询是否有相同用户名的用户
        UmsAdminExample example = new UmsAdminExample();
        // 创建查询条件：用户名等于要注册的用户名
        example.createCriteria().andUsernameEqualTo(umsAdmin.getUsername());
        // 执行查询操作
        List<UmsAdmin> umsAdminList = adminMapper.selectByExample(example);
        // 如果查询结果不为空，说明用户名已存在，返回null表示注册失败
        if (umsAdminList.size() > 0) {
            return null;
        }
        // 将密码进行加密操作，提高安全性
        String encodePassword = passwordEncoder.encode(umsAdmin.getPassword());
        // 设置加密后的密码
        umsAdmin.setPassword(encodePassword);
        // 将管理员信息插入数据库
        adminMapper.insert(umsAdmin);
        // 返回创建成功的管理员对象
        return umsAdmin;
    }

    /**
     * 后台管理员登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 生成的JWT token，登录失败则返回null
     */
    @Override
    public String login(String username, String password) {
        String token = null;
        //密码需要客户端加密后传递
        try {
            // 根据用户名获取用户详情信息
            UserDetails userDetails = loadUserByUsername(username);
            // 验证密码是否正确
            if(!passwordEncoder.matches(password,userDetails.getPassword())){
                Asserts.fail("密码不正确");
            }
            // 检查账号是否被禁用
            if(!userDetails.isEnabled()){
                Asserts.fail("帐号已被禁用");
            }
            // 创建认证令牌并设置到安全上下文中
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // 生成JWT令牌
            token = jwtTokenUtil.generateToken(userDetails);
            // 更新登录时间的代码已被注释掉
//            updateLoginTimeByUsername(username);
            // 记录登录日志
            insertLoginLog(username);
        } catch (AuthenticationException e) {
            // 捕获并记录登录过程中的异常
            LOGGER.warn("登录异常:{}", e.getMessage());
        }

//        response.setHeader("Authorization", "Bearer " + token);

        // 返回生成的令牌
        return token;
    }


    /**
     * 添加登录记录
     * 该方法用于在用户成功登录后记录登录信息，包括登录用户ID、登录时间和IP地址
     *
     * @param username 用户名，用于获取对应的管理员信息
     */
    private void insertLoginLog(String username) {
        // 根据用户名获取管理员信息
        UmsAdmin admin = getAdminByUsername(username);
        // 如果管理员不存在，直接返回，不记录日志
        if(admin==null) return;
        // 创建新的登录日志对象
        UmsAdminLoginLog loginLog = new UmsAdminLoginLog();
        // 设置管理员ID
        loginLog.setAdminId(admin.getId());
        // 设置登录时间为当前时间
        loginLog.setCreateTime(new Date());
        // 获取当前请求的属性
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 从请求属性中获取HTTP请求对象
        HttpServletRequest request = attributes.getRequest();
        // 获取并设置登录IP地址
        loginLog.setIp(RequestUtil.getRequestIp(request));
        // 将登录日志插入数据库
        loginLogMapper.insert(loginLog);
    }


    /**
     * 根据用户名修改登录时间
     * 该方法用于在用户成功登录后更新最后登录时间
     *
     * @param username 需要更新登录时间的用户名
     */
    private void updateLoginTimeByUsername(String username) {
        // 创建一个新的管理员对象，用于存储要更新的字段
        UmsAdmin record = new UmsAdmin();
        // 设置登录时间为当前时间
        record.setLoginTime(new Date());
        // 创建查询条件对象
        UmsAdminExample example = new UmsAdminExample();
        // 设置查询条件：用户名等于传入的username
        example.createCriteria().andUsernameEqualTo(username);
        // 执行选择性更新操作，只更新非空字段（这里只更新登录时间）
        adminMapper.updateByExampleSelective(record, example);
    }

    /**
     * 刷新token的功能
     * 该方法用于当原token即将过期时，生成一个新的token
     *
     * @param oldToken 旧的JWT token
     * @return 刷新后的新JWT token
     */
    @Override
    public String refreshToken(String oldToken) {
        // 调用JWT工具类的刷新token方法，生成新的token
        return jwtTokenUtil.refreshHeadToken(oldToken);
    }

    /**
     * 根据管理员ID获取管理员信息
     *
     * @param id 管理员ID
     * @return 对应ID的管理员对象，如果不存在则返回null
     */
    @Override
    public UmsAdmin getItem(Long id) {
        // 通过主键ID直接查询管理员信息
        return adminMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据关键字分页查询后台管理员列表
     *
     * @param keyword 查询关键字，可以是用户名或昵称的一部分
     * @param pageSize 每页显示的记录数
     * @param pageNum 当前页码
     * @return 符合条件的管理员列表
     */
    @Override
    public List<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum) {
        // 设置分页参数，开始分页查询
        PageHelper.startPage(pageNum, pageSize);
        // 创建查询条件对象
        UmsAdminExample example = new UmsAdminExample();
        // 获取查询条件构造器
        UmsAdminExample.Criteria criteria = example.createCriteria();
        // 如果关键字不为空，添加模糊查询条件
        if (!StrUtil.isEmpty(keyword)) {
            // 添加用户名模糊查询条件
            criteria.andUsernameLike("%" + keyword + "%");
            // 添加昵称模糊查询条件（使用OR连接）
            example.or(example.createCriteria().andNickNameLike("%" + keyword + "%"));
        }
        // 执行查询并返回结果
        return adminMapper.selectByExample(example);
    }

    /**
     * 更新后台管理员信息
     *
     * @param id 要更新的管理员ID
     * @param admin 包含更新信息的管理员对象
     * @return 更新成功返回1，失败返回0
     */
    @Override
    public int update(Long id, UmsAdmin admin) {
        // 设置管理员ID，确保更新的是指定ID的记录
        admin.setId(id);
        // 获取原始管理员信息
        UmsAdmin rawAdmin = adminMapper.selectByPrimaryKey(id);
        if (rawAdmin == null){
            Asserts.fail("未找到对应的管理员信息");
        }

        // 判断密码是否需要更新
        if(rawAdmin.getPassword().equals(admin.getPassword())){
            // 与原加密密码相同的不需要修改
            admin.setPassword(null);
        }else{
            // 与原加密密码不同的需要加密修改
            if(StrUtil.isEmpty(admin.getPassword())){
                // 如果新密码为空，则不更新密码
                admin.setPassword(null);
            }else{
                // 如果新密码不为空，则进行加密后更新
                admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            }
        }
        // 执行选择性更新操作，只更新非空字段
        int count = adminMapper.updateByPrimaryKeySelective(admin);
        // 删除该管理员的缓存
        getCacheService().delAdmin(id);
        // 返回更新结果
        return count;
    }

    /**
     * 删除指定ID的后台管理员
     *
     * @param id 要删除的管理员ID
     * @return 删除成功返回1，失败返回0
     */
    @Override
    public int delete(Long id) {
        // 从数据库中删除指定ID的管理员记录
        int count = adminMapper.deleteByPrimaryKey(id);
        // 删除缓存中的管理员信息
        getCacheService().delAdmin(id);
        // 删除缓存中该管理员的资源列表
        getCacheService().delResourceList(id);
        // 返回删除结果
        return count;
    }

    /**
     * 更新管理员的角色关系
     * 该方法用于重新分配指定管理员的角色权限
     *
     * @param adminId 管理员ID
     * @param roleIds 要分配给管理员的角色ID列表
     * @return 分配的角色数量
     */
    @Override
    public int updateRole(Long adminId, List<Long> roleIds) {
        // 计算角色数量，如果roleIds为null则为0，否则为roleIds的大小
        int count = roleIds == null ? 0 : roleIds.size();
        // 先删除原来的关系
        UmsAdminRoleRelationExample adminRoleRelationExample = new UmsAdminRoleRelationExample();
        // 设置查询条件：管理员ID等于传入的adminId
        adminRoleRelationExample.createCriteria().andAdminIdEqualTo(adminId);
        // 删除该管理员的所有角色关系
        adminRoleRelationMapper.deleteByExample(adminRoleRelationExample);
        // 建立新关系
        if (!CollectionUtils.isEmpty(roleIds)) {
            // 创建角色关系列表
            List<UmsAdminRoleRelation> list = new ArrayList<>();
            // 遍历角色ID列表
            for (Long roleId : roleIds) {
                // 创建新的角色关系对象
                UmsAdminRoleRelation roleRelation = new UmsAdminRoleRelation();
                // 设置管理员ID
                roleRelation.setAdminId(adminId);
                // 设置角色ID
                roleRelation.setRoleId(roleId);
                // 将角色关系添加到列表中
                list.add(roleRelation);
            }
            // 批量插入角色关系
            adminRoleRelationDao.insertList(list);
        }
        // 删除该管理员的资源列表缓存
        getCacheService().delResourceList(adminId);
        // 返回分配的角色数量
        return count;
    }

    /**
     * 根据管理员ID获取该管理员拥有的所有角色列表
     *
     * @param adminId 管理员ID
     * @return 该管理员拥有的角色列表
     */
    @Override
    public List<UmsRole> getRoleList(Long adminId) {
        // 调用adminRoleRelationDao的getRoleList方法，根据管理员ID查询并返回其拥有的角色列表
        return adminRoleRelationDao.getRoleList(adminId);
    }

    /**
     * 获取指定管理员的所有可访问资源列表
     * 该方法首先尝试从缓存中获取资源列表，如果缓存中不存在，则从数据库中查询，
     * 并将查询结果存入缓存，以提高后续访问效率。
     *
     * @param adminId 管理员ID
     * @return 该管理员可访问的资源列表
     */
    @Override
    public List<UmsResource> getResourceList(Long adminId) {
        //先从缓存中获取数据
        List<UmsResource> resourceList = getCacheService().getResourceList(adminId);
        //判断缓存中是否存在该管理员的资源列表
        if(CollUtil.isNotEmpty(resourceList)){
            //如果缓存中存在资源列表，直接返回缓存数据
            return  resourceList;
        }
        //缓存中没有从数据库中获取
        resourceList = adminRoleRelationDao.getResourceList(adminId);
        //判断从数据库获取的资源列表是否为空
        if(CollUtil.isNotEmpty(resourceList)){
            //将数据库中的数据存入缓存中，提高下次查询效率
            getCacheService().setResourceList(adminId,resourceList);
        }
        //返回查询到的资源列表，如果数据库中也没有，则返回空列表
        return resourceList;
    }

    /**
     * 更新后台管理员密码
     * 该方法用于验证旧密码并更新为新密码，同时清除缓存中的管理员信息
     *
     * @param param 包含用户名、旧密码和新密码的参数对象
     * @return 更新结果：1表示成功，-1表示参数不完整，-2表示用户不存在，-3表示旧密码错误
     */
    @Override
    public int updatePassword(UpdateAdminPasswordParam param) {
        // 验证参数完整性，如果用户名、旧密码或新密码为空，返回-1
        if(StrUtil.isEmpty(param.getUsername())
                ||StrUtil.isEmpty(param.getOldPassword())
                ||StrUtil.isEmpty(param.getNewPassword())){
            return -1;
        }
        // 创建查询条件对象
        UmsAdminExample example = new UmsAdminExample();
        // 设置查询条件：用户名等于传入参数中的用户名
        example.createCriteria().andUsernameEqualTo(param.getUsername());
        // 执行查询操作
        List<UmsAdmin> adminList = adminMapper.selectByExample(example);
        // 判断查询结果是否为空，如果为空表示用户不存在，返回-2
        if(CollUtil.isEmpty(adminList)){
            return -2;
        }
        // 获取查询结果的第一条数据
        UmsAdmin umsAdmin = adminList.get(0);
        // 验证旧密码是否正确，使用passwordEncoder进行匹配
        if(!passwordEncoder.matches(param.getOldPassword(),umsAdmin.getPassword())){
            // 旧密码不正确，返回-3
            return -3;
        }
        // 设置新密码，使用passwordEncoder进行加密
        umsAdmin.setPassword(passwordEncoder.encode(param.getNewPassword()));
        // 更新管理员信息到数据库
        adminMapper.updateByPrimaryKey(umsAdmin);
        // 删除缓存中的管理员信息，确保下次获取时能获取到最新数据
        getCacheService().delAdmin(umsAdmin.getId());
        // 返回1表示更新成功
        return 1;
    }

    /**
     * 根据用户名加载用户详情信息
     * 该方法实现了Spring Security的UserDetailsService接口，用于身份验证过程中加载用户信息
     *
     * @param username 要查询的用户名
     * @return 包含用户信息和权限的UserDetails对象
     * @throws UsernameNotFoundException 当用户名不存在时抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username){
        //获取用户信息
        UmsAdmin admin = getAdminByUsername(username);
        //判断用户是否存在
        if (admin != null) {
            //获取该用户的资源权限列表
            List<UmsResource> resourceList = getResourceList(admin.getId());
            //创建并返回包含用户信息和权限的UserDetails对象
            return new AdminUserDetails(admin,resourceList);
        }
        //如果用户不存在，抛出用户名未找到异常
        throw new UsernameNotFoundException("用户名或密码错误");
    }

    /**
     * 获取后台管理员缓存服务实例
     * 该方法用于获取UmsAdminCacheService的实例，以便进行后台管理员相关的缓存操作。
     * 通过Spring的工具类SpringUtil动态获取Bean实例，实现了缓存服务的解耦。
     *
     * @return UmsAdminCacheService 返回后台管理员缓存服务的实例
     */
    @Override
    public UmsAdminCacheService getCacheService() {
        // 使用SpringUtil工具类从Spring容器中获取UmsAdminCacheService的实例
        // 这种方式可以避免直接依赖注入可能造成的循环依赖问题
        return SpringUtil.getBean(UmsAdminCacheService.class);
    }

    /**
     * 退出登录操作
     * 该方法用于清除指定用户名对应管理员的缓存信息，实现安全退出功能。
     * 包括清除管理员基本信息缓存和资源列表缓存，确保下次登录时能获取到最新数据。
     *
     * @param username 需要退出登录的用户名
     */
    @Override
    public void logout(String username) {
        // 根据用户名从缓存中获取管理员对象
        UmsAdmin admin = getCacheService().getAdmin(username);
        // 删除缓存中的管理员基本信息
        getCacheService().delAdmin(admin.getId());
        // 删除缓存中的管理员资源列表
        getCacheService().delResourceList(admin.getId());
    }
}
