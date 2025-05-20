package com.lzy.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.lzy.mall.dao.UmsRoleDao;
import com.lzy.mall.mapper.UmsRoleMapper;
import com.lzy.mall.mapper.UmsRoleMenuRelationMapper;
import com.lzy.mall.mapper.UmsRoleResourceRelationMapper;
import com.lzy.mall.model.*;
import com.lzy.mall.service.UmsAdminCacheService;
import com.lzy.mall.service.UmsRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 后台角色管理Service实现类
 * 实现了UmsRoleService接口，提供角色的增删改查、菜单和资源的分配与查询等功能。
 * 通过整合Dao和Mapper，实现对角色相关数据的业务处理和数据库操作。
 */
@Service
public class UmsRoleServiceImpl implements UmsRoleService {
    @Autowired
    private UmsRoleMapper roleMapper;
    @Autowired
    private UmsRoleMenuRelationMapper roleMenuRelationMapper;
    @Autowired
    private UmsRoleResourceRelationMapper roleResourceRelationMapper;
    @Autowired
    private UmsRoleDao roleDao;
    @Autowired
    private UmsAdminCacheService adminCacheService;

    /**
     * 添加角色
     * @param role 角色对象
     * @return 添加结果，成功返回1
     */
    @Override
    public int create(UmsRole role) {
        // 设置角色创建时间
        role.setCreateTime(new Date());
        // 初始化管理员数量为0
        role.setAdminCount(0);
        // 初始化排序字段为0
        role.setSort(0);
        // 插入角色数据到数据库
        return roleMapper.insert(role);
    }

    /**
     * 修改角色信息
     * @param id 角色ID
     * @param role 角色对象
     * @return 修改结果，成功返回1
     */
    @Override
    public int update(Long id, UmsRole role) {
        // 设置要修改的角色ID
        role.setId(id);
        // 更新角色信息（只更新非空字段）
        return roleMapper.updateByPrimaryKeySelective(role);
    }

    /**
     * 批量删除角色
     * @param ids 角色ID列表
     * @return 删除的数量
     */
    @Override
    public int delete(List<Long> ids) {
        // 构建删除条件
        UmsRoleExample example = new UmsRoleExample();
        example.createCriteria().andIdIn(ids);
        // 执行批量删除
        int count = roleMapper.deleteByExample(example);
        // 删除相关角色的缓存资源列表
        adminCacheService.delResourceListByRoleIds(ids);
        return count;
    }

    /**
     * 获取所有角色列表
     * @return 角色列表
     */
    @Override
    public List<UmsRole> list() {
        // 查询所有角色
        return roleMapper.selectByExample(new UmsRoleExample());
    }

    /**
     * 分页获取角色列表
     * @param keyword 查询关键字
     * @param pageSize 每页数量
     * @param pageNum 页码
     * @return 角色列表
     */
    @Override
    public List<UmsRole> list(String keyword, Integer pageSize, Integer pageNum) {
        // 设置分页参数
        PageHelper.startPage(pageNum, pageSize);
        UmsRoleExample example = new UmsRoleExample();
        // 如果有关键字则按名称模糊查询
        if (!StrUtil.isEmpty(keyword)) {
            example.createCriteria().andNameLike("%" + keyword + "%");
        }
        // 查询角色列表
        return roleMapper.selectByExample(example);
    }

    /**
     * 根据管理员ID获取对应菜单
     * @param adminId 管理员ID
     * @return 菜单列表
     */
    @Override
    public List<UmsMenu> getMenuList(Long adminId) {
        // 查询管理员拥有的菜单列表
        return roleDao.getMenuList(adminId);
    }

    /**
     * 获取角色相关菜单
     * @param roleId 角色ID
     * @return 菜单列表
     */
    @Override
    public List<UmsMenu> listMenu(Long roleId) {
        // 查询角色拥有的菜单列表
        return roleDao.getMenuListByRoleId(roleId);
    }

    /**
     * 获取角色相关资源
     * @param roleId 角色ID
     * @return 资源列表
     */
    @Override
    public List<UmsResource> listResource(Long roleId) {
        // 查询角色拥有的资源列表
        return roleDao.getResourceListByRoleId(roleId);
    }

    /**
     * 给角色分配菜单
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 分配结果
     */
    @Override
    public int allocMenu(Long roleId, List<Long> menuIds) {
        // 先删除原有的角色菜单关系
        UmsRoleMenuRelationExample example = new UmsRoleMenuRelationExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        roleMenuRelationMapper.deleteByExample(example);
        // 批量插入新的角色菜单关系
        for (Long menuId : menuIds) {
            UmsRoleMenuRelation relation = new UmsRoleMenuRelation();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            roleMenuRelationMapper.insert(relation);
        }
        return menuIds.size();
    }

    /**
     * 给角色分配资源
     * @param roleId 角色ID
     * @param resourceIds 资源ID列表
     * @return 分配结果
     */
    @Override
    public int allocResource(Long roleId, List<Long> resourceIds) {
        // 先删除原有的角色资源关系
        UmsRoleResourceRelationExample example = new UmsRoleResourceRelationExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        roleResourceRelationMapper.deleteByExample(example);
        // 批量插入新的角色资源关系
        for (Long resourceId : resourceIds) {
            UmsRoleResourceRelation relation = new UmsRoleResourceRelation();
            relation.setRoleId(roleId);
            relation.setResourceId(resourceId);
            roleResourceRelationMapper.insert(relation);
        }
        // 删除该角色相关的缓存资源列表
        adminCacheService.delResourceListByRole(roleId);
        return resourceIds.size();
    }
}
