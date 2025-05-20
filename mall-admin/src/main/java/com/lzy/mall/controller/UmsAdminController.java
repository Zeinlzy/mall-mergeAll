package com.lzy.mall.controller;

import cn.hutool.core.collection.CollUtil;
import com.lzy.mall.common.api.CommonPage;
import com.lzy.mall.common.api.CommonResult;
import com.lzy.mall.dto.UmsAdminLoginParam;
import com.lzy.mall.dto.UmsAdminParam;
import com.lzy.mall.dto.UpdateAdminPasswordParam;
import com.lzy.mall.model.UmsAdmin;
import com.lzy.mall.model.UmsRole;
import com.lzy.mall.service.UmsAdminService;
import com.lzy.mall.service.UmsRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 后台用户管理Controller
 * 用于处理后台用户的注册、登录、信息获取、管理、角色分配等相关请求。
 */
@Controller
@Tag(name = "UmsAdminController", description = "后台用户管理")
@RequestMapping("/admin")
public class UmsAdminController {

    // 从配置文件中读取 JWT token 的请求头名称
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;

    // 从配置文件中读取 JWT token 的头部前缀（如 "Bearer "）
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    // 注入后台用户服务
    @Autowired
    private UmsAdminService adminService;

    // 注入后台角色服务
    @Autowired
    private UmsRoleService roleService;

    /**
     * 用户注册接口
     * 处理用户注册请求，接收用户注册参数并调用服务进行注册。
     * @param umsAdminParam 用户注册参数DTO，包含用户名、密码等信息
     * @return 注册结果，成功返回新创建的用户信息，失败返回通用失败结果
     */
    @Operation(summary = "用户注册")  //测试成功
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<UmsAdmin> register(@Validated @RequestBody UmsAdminParam umsAdminParam) {
        // 调用Service层执行用户注册逻辑
        UmsAdmin umsAdmin = adminService.register(umsAdminParam);
        // 判断注册是否成功（Service返回null表示注册失败，可能是用户名已存在等）
        if (umsAdmin == null) {
            return CommonResult.failed(); // 注册失败，返回通用失败结果
        }
        return CommonResult.success(umsAdmin); // 注册成功，返回成功结果和用户对象
    }

    /**
     * 用户登录接口
     * 处理用户登录请求，验证用户名密码并返回JWT token。
     * @param umsAdminLoginParam 用户登录参数DTO，包含用户名和密码
     * @return 登录结果，成功返回包含token和tokenHead的Map，失败返回验证失败信息
     */
    @Operation(summary = "登录以后返回token")  //测试成功
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult login(@Validated @RequestBody UmsAdminLoginParam umsAdminLoginParam, HttpServletResponse response) {
        // 调用Service层执行登录验证并生成JWT token
        String token = adminService.login(umsAdminLoginParam.getUsername(), umsAdminLoginParam.getPassword());
        // 判断登录是否成功（Service返回null表示用户名或密码错误）
        if (token == null) {
            return CommonResult.validateFailed("用户名或密码错误"); // 登录失败，返回验证失败信息
        }
        // 登录成功，构建返回结果Map
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token); // 将生成的token放入Map
        tokenMap.put("tokenHead", tokenHead); // 将token头部前缀放入Map

        response.setHeader(tokenHeader, tokenHead + token);

        return CommonResult.success(tokenMap); // 返回成功结果和token信息
    }

    /**
     * 刷新JWT token
     * 根据旧的token刷新生成新的token，延长token有效期。
     * @param request HttpServletRequest对象，用于获取请求头中的旧token
     * @return 刷新结果，成功返回新的token信息，失败返回错误信息（如token过期）
     */
    @Operation(summary = "刷新token")  //测试成功
    @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult refreshToken(HttpServletRequest request) {
        // 从请求头中获取旧的token
        String token = request.getHeader(tokenHeader);
        // 调用Service层尝试刷新token
        String refreshToken = adminService.refreshToken(token);
        // 判断刷新是否成功（Service返回null表示旧token无效或已过期）
        if (refreshToken == null) {
            return CommonResult.failed("token已经过期！"); // 刷新失败，返回过期提示
        }
        // 刷新成功，构建返回结果Map
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", refreshToken); // 将新生成的token放入Map
        tokenMap.put("tokenHead", tokenHead); // 将token头部前缀放入Map
        return CommonResult.success(tokenMap); // 返回成功结果和新token信息
    }

    /**
     * 获取当前登录用户信息
     * 根据Principal对象获取当前用户名，并查询用户详细信息、菜单列表和角色列表。
     * @param principal Spring Security注入的当前用户认证信息
     * @return 用户详细信息（用户名、图标、菜单、角色）结果
     */
    @Operation(summary = "获取当前登录用户信息")  //测试成功
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult getAdminInfo(Principal principal) {
        // 检查Principal是否为null，如果为null说明用户未认证（未登录或token无效）
        if(principal==null){
            return CommonResult.unauthorized(null); // 返回未认证的错误结果
        }
        // 从Principal中获取当前登录的用户名
        String username = principal.getName();
        // 调用Service层根据用户名查询用户对象
        UmsAdmin umsAdmin = adminService.getAdminByUsername(username);
        // 构建存储用户详细信息的Map
        Map<String, Object> data = new HashMap<>();
        data.put("username", umsAdmin.getUsername()); // 添加用户名
        // 调用Service层获取该用户拥有的菜单列表（通常通过角色关联获取）
        data.put("menus", roleService.getMenuList(umsAdmin.getId()));
        data.put("icon", umsAdmin.getIcon()); // 添加用户头像（或其他图标）

        // 调用Service层获取该用户拥有的角色列表
        List<UmsRole> roleList = adminService.getRoleList(umsAdmin.getId());
        // 检查角色列表是否非空
        if(CollUtil.isNotEmpty(roleList)){
            // 使用Stream API将角色对象列表转换为角色名称字符串列表
            List<String> roles = roleList.stream().map(UmsRole::getName).collect(Collectors.toList());
            data.put("roles",roles); // 将角色名称列表添加到Map
        }
        // 返回成功结果，包含用户详细信息
        return CommonResult.success(data);
    }

    /**
     * 用户登出接口
     * 处理用户登出请求，执行清理操作（如使token失效）。
     * @param principal Spring Security注入的当前用户认证信息
     * @return 登出成功结果
     */
    @Operation(summary = "登出功能")  //测试成功
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult logout(Principal principal) {
        // 调用Service层执行登出逻辑（例如：清除缓存、使token失效等）
        adminService.logout(principal.getName());
        return CommonResult.success(null); // 返回登出成功结果
    }

    /**
     * 分页获取后台用户列表
     * 根据关键字（用户名或姓名）分页查询后台用户信息。
     * @param keyword 查询关键字（可选）
     * @param pageSize 每页大小，默认为5
     * @param pageNum 当前页码，默认为1
     * @return 分页的用户列表结果
     */
    @Operation(summary = "根据用户名或姓名分页获取用户列表")  //测试成功
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<UmsAdmin>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                   @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        // 调用Service层执行分页查询用户列表
        List<UmsAdmin> adminList = adminService.list(keyword, pageSize, pageNum);
        // 将查询结果封装为CommonPage分页对象并返回成功结果
        return CommonResult.success(CommonPage.restPage(adminList));
    }

    /**
     * 获取指定用户信息
     * 根据用户ID查询单个用户的详细信息。
     * @param id 用户ID
     * @return 指定用户信息结果
     */
    @Operation(summary = "获取指定用户信息")  //测试成功
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<UmsAdmin> getItem(@PathVariable Long id) {
        // 调用Service层根据ID查询单个用户
        UmsAdmin admin = adminService.getItem(id);
        return CommonResult.success(admin); // 返回查询到的用户对象
    }

    /**
     * 修改指定用户信息
     * 根据用户ID修改用户的基本信息。
     * @param id 用户ID
     * @param admin 包含更新信息的用户对象
     * @return 更新结果，成功返回更新条数，失败返回通用失败信息
     */
    @Operation(summary = "修改指定用户信息")  //测试成功
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult update(@PathVariable Long id, @RequestBody UmsAdmin admin) {
        // 调用Service层执行用户信息更新操作
        int count = adminService.update(id, admin);
        // 判断更新是否成功（返回更新的记录数，大于0表示成功）
        if (count > 0) {
            return CommonResult.success(count); // 更新成功，返回更新条数
        }
        return CommonResult.failed(); // 更新失败，返回通用失败信息
    }

    /**
     * 修改指定用户密码
     * 接收用户ID、旧密码和新密码，执行密码修改操作。
     * @param updatePasswordParam 密码修改参数DTO
     * @return 修改结果，成功返回更新条数，失败返回特定错误信息（如参数不合法、用户不存在、旧密码错误）
     */
    @Operation(summary = "修改指定用户密码")  //测试成功
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updatePassword(@Validated @RequestBody UpdateAdminPasswordParam updatePasswordParam) {
        // 调用Service层执行密码修改逻辑，返回状态码
        int status = adminService.updatePassword(updatePasswordParam);
        // 根据状态码判断修改结果并返回相应信息
        if (status > 0) {
            return CommonResult.success(status); // 修改成功
        } else if (status == -1) {
            return CommonResult.failed("提交参数不合法"); // 参数校验失败或不符合规则
        } else if (status == -2) {
            return CommonResult.failed("找不到该用户"); // 根据用户名找不到用户
        } else if (status == -3) {
            return CommonResult.failed("旧密码错误"); // 提供的旧密码不正确
        } else {
            return CommonResult.failed(); // 其他未知失败情况
        }
    }

    /**
     * 删除指定用户信息
     * 根据用户ID删除用户。
     * @param id 用户ID
     * @return 删除结果，成功返回删除条数，失败返回通用失败信息
     */
    @Operation(summary = "删除指定用户信息") //测试成功
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@PathVariable Long id) {
        // 调用Service层执行用户删除操作
        int count = adminService.delete(id);
        // 判断删除是否成功（返回删除的记录数，大于0表示成功）
        if (count > 0) {
            return CommonResult.success(count); // 删除成功，返回删除条数
        }
        return CommonResult.failed(); // 删除失败，返回通用失败信息
    }

    /**
     * 修改帐号状态
     * 根据用户ID和状态值更新用户的启用/禁用状态。
     * @param id 用户ID
     * @param status 状态值（通常0表示禁用，1表示启用）
     * @return 更新结果，成功返回更新条数，失败返回通用失败信息
     */
    @Operation(summary = "修改帐号状态")  //测试成功
    @RequestMapping(value = "/updateStatus/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateStatus(@PathVariable Long id,@RequestParam(value = "status") Integer status) {
        // 创建一个临时的UmsAdmin对象，用于只更新状态字段
        UmsAdmin umsAdmin = new UmsAdmin();
        umsAdmin.setStatus(status); // 设置要更新的状态值
        // 调用Service层执行部分更新（只更新状态）
        int count = adminService.update(id,umsAdmin);
        // 判断更新是否成功
        if (count > 0) {
            return CommonResult.success(count); // 更新成功
        }
        return CommonResult.failed(); // 更新失败
    }

    /**
     * 给用户分配角色
     * 根据用户ID和角色ID列表，更新用户的角色关联。
     * @param adminId 用户ID
     * @param roleIds 角色ID列表
     * @return 分配结果，成功返回操作条数（关联表的变化），失败返回通用失败信息
     */
    @Operation(summary = "给用户分配角色")
    @RequestMapping(value = "/role/update", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateRole(@RequestParam("adminId") Long adminId,
                                   @RequestParam("roleIds") List<Long> roleIds) {
        // 调用Service层执行角色分配操作
        int count = adminService.updateRole(adminId, roleIds);
        // 判断操作是否成功（返回操作的记录数，大于等于0通常表示成功，因为可能清空所有角色导致操作数为0）
        if (count >= 0) {
            return CommonResult.success(count); // 分配成功
        }
        return CommonResult.failed(); // 分配失败
    }

    /**
     * 获取指定用户的角色列表
     * 根据用户ID查询该用户拥有的所有角色。
     * @param adminId 用户ID
     * @return 角色列表结果
     */
    @Operation(summary = "获取指定用户的角色")  //测试成功
    @RequestMapping(value = "/role/{adminId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UmsRole>> getRoleList(@PathVariable Long adminId) {
        // 调用Service层查询用户的角色列表
        List<UmsRole> roleList = adminService.getRoleList(adminId);
        return CommonResult.success(roleList); // 返回查询到的角色列表
    }

}
