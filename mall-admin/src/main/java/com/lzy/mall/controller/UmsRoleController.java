package com.lzy.mall.controller;

import com.lzy.mall.common.api.CommonPage;
import com.lzy.mall.common.api.CommonResult;
import com.lzy.mall.model.*;
import com.lzy.mall.service.UmsRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UmsRoleController
 * 后台用户角色管理Controller，负责处理与角色相关的请求，包括角色的增删改查、分配菜单和资源等。
 * 通过调用UmsRoleService实现具体的业务逻辑。
 */
@Controller
@Tag(name = "UmsRoleController", description = "后台用户角色管理")
@RequestMapping("/role")
public class UmsRoleController {
    @Autowired
    private UmsRoleService roleService;

    /**
     * 添加角色
     * @param role 角色对象
     * @return 操作结果
     */
    @Operation(summary = "添加角色")  //测试成功
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult create(@RequestBody UmsRole role) {
        // 调用service层添加角色
        int count = roleService.create(role);
        if (count > 0) {
            // 添加成功，返回成功结果
            return CommonResult.success(count);
        }
        // 添加失败，返回失败结果
        return CommonResult.failed();
    }

    /**
     * 修改角色
     * @param id 角色ID
     * @param role 角色对象
     * @return 操作结果
     */
    @Operation(summary = "修改角色")  //测试成功
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult update(@PathVariable Long id, @RequestBody UmsRole role) {
        // 调用service层修改角色
        int count = roleService.update(id, role);
        if (count > 0) {
            // 修改成功，返回成功结果
            return CommonResult.success(count);
        }
        // 修改失败，返回失败结果
        return CommonResult.failed();
    }

    /**
     * 批量删除角色
     * @param ids 角色ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除角色")  //测试成功
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@RequestParam("ids") List<Long> ids) {
        // 调用service层批量删除角色
        int count = roleService.delete(ids);
        if (count > 0) {
            // 删除成功，返回成功结果
            return CommonResult.success(count);
        }
        // 删除失败，返回失败结果
        return CommonResult.failed();
    }

    /**
     * 获取所有角色
     * @return 所有角色列表
     */
    @Operation(summary = "获取所有角色")  //测试成功
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UmsRole>> listAll() {
        // 查询所有角色
        List<UmsRole> roleList = roleService.list();
        return CommonResult.success(roleList);
    }

    /**
     * 根据角色名称分页获取角色列表
     * @param keyword 角色名称关键字（可选）
     * @param pageSize 每页数量
     * @param pageNum 页码
     * @return 分页后的角色列表
     */
    @Operation(summary = "根据角色名称分页获取角色列表")  //测试成功
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<UmsRole>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                  @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                  @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        // 分页查询角色列表
        List<UmsRole> roleList = roleService.list(keyword, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(roleList));
    }

    /**
     * 修改角色状态
     * @param id 角色ID
     * @param status 状态值
     * @return 操作结果
     */
    @Operation(summary = "修改角色状态")  //测试成功
    @RequestMapping(value = "/updateStatus/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateStatus(@PathVariable Long id, @RequestParam(value = "status") Integer status) {
        // 创建角色对象并设置状态
        UmsRole umsRole = new UmsRole();
        umsRole.setStatus(status);
        // 调用service层更新角色状态
        int count = roleService.update(id, umsRole);
        if (count > 0) {
            // 更新成功，返回成功结果
            return CommonResult.success(count);
        }
        // 更新失败，返回失败结果
        return CommonResult.failed();
    }

    /**
     * 获取角色相关菜单
     * @param roleId 角色ID
     * @return 角色对应的菜单列表
     */
    @Operation(summary = "获取角色相关菜单")  //测试成功
    @RequestMapping(value = "/listMenu/{roleId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UmsMenu>> listMenu(@PathVariable Long roleId) {
        // 查询角色对应的菜单
        List<UmsMenu> roleList = roleService.listMenu(roleId);
        return CommonResult.success(roleList);
    }

    /**
     * 获取角色相关资源
     * @param roleId 角色ID
     * @return 角色对应的资源列表
     */
    @Operation(summary = "获取角色相关资源")  //测试成功
    @RequestMapping(value = "/listResource/{roleId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UmsResource>> listResource(@PathVariable Long roleId) {
        // 查询角色对应的资源
        List<UmsResource> roleList = roleService.listResource(roleId);
        return CommonResult.success(roleList);
    }

    /**
     * 给角色分配菜单
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 操作结果
     */
    @Operation(summary = "给角色分配菜单")  //测试成功
    @RequestMapping(value = "/allocMenu", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult allocMenu(@RequestParam Long roleId, @RequestParam List<Long> menuIds) {
        // 调用service层分配菜单
        int count = roleService.allocMenu(roleId, menuIds);
        return CommonResult.success(count);
    }

    /**
     * 给角色分配资源
     * @param roleId 角色ID
     * @param resourceIds 资源ID列表
     * @return 操作结果
     */
    @Operation(summary = "给角色分配资源")  //测试成功
    @RequestMapping(value = "/allocResource", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult allocResource(@RequestParam Long roleId, @RequestParam List<Long> resourceIds) {
        // 调用service层分配资源
        int count = roleService.allocResource(roleId, resourceIds);
        return CommonResult.success(count);
    }

}
