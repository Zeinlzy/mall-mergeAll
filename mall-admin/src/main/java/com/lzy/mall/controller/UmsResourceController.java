package com.lzy.mall.controller;

import com.lzy.mall.common.api.CommonPage;
import com.lzy.mall.common.api.CommonResult;
import com.lzy.mall.model.UmsResource;
import com.lzy.mall.security.component.DynamicSecurityMetadataSource;
import com.lzy.mall.service.UmsResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 后台资源管理控制器
 * 负责处理系统资源的CRUD操作，包括资源的创建、更新、删除、查询等功能
 * 资源通常指系统中的菜单、按钮、API接口等权限控制对象
 */
@Controller
@Tag(name = "UmsResourceController", description = "后台资源管理")
@RequestMapping("/resource")
public class UmsResourceController {

    /**
     * 资源服务层接口，处理资源相关的业务逻辑
     */
    @Autowired
    private UmsResourceService resourceService;

    /**
     * 动态安全元数据源，用于Spring Security权限控制
     * 当资源发生变更时需要清空缓存以确保权限配置实时生效
     */
    @Autowired
    private DynamicSecurityMetadataSource dynamicSecurityMetadataSource;

    /**
     * 创建新的后台资源
     *
     * @param umsResource 资源对象，包含资源名称、URL、分类等信息
     * @return CommonResult 统一响应结果
     *         - 成功时返回影响的记录数
     *         - 失败时返回失败状态
     */
    @Operation(summary = "添加后台资源")  //测试成功
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult create(@RequestBody UmsResource umsResource) {
        // 调用服务层创建资源，返回影响的记录数
        int count = resourceService.create(umsResource);

        // 清空动态权限缓存，确保新增的资源权限配置立即生效
        dynamicSecurityMetadataSource.clearDataSource();

        // 根据影响记录数判断操作是否成功
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 更新指定ID的后台资源
     *
     * @param id 资源ID，用于指定要更新的资源
     * @param umsResource 包含更新信息的资源对象
     * @return CommonResult 统一响应结果
     *         - 成功时返回影响的记录数
     *         - 失败时返回失败状态
     */
    @Operation(summary = "修改后台资源")  //测试成功
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult update(@PathVariable Long id,
                               @RequestBody UmsResource umsResource) {
        // 调用服务层更新指定ID的资源
        int count = resourceService.update(id, umsResource);

        // 清空动态权限缓存，确保修改后的资源权限配置立即生效
        dynamicSecurityMetadataSource.clearDataSource();

        // 根据影响记录数判断更新是否成功
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 根据ID获取资源详细信息
     *
     * @param id 资源ID
     * @return CommonResult<UmsResource> 包含资源详细信息的统一响应结果
     */
    @Operation(summary = "根据ID获取资源详情")  //测试成功
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<UmsResource> getItem(@PathVariable Long id) {
        // 调用服务层根据ID查询资源详情
        UmsResource umsResource = resourceService.getItem(id);

        // 直接返回成功结果，包含查询到的资源对象
        return CommonResult.success(umsResource);
    }

    /**
     * 根据ID删除后台资源
     *
     * @param id 要删除的资源ID
     * @return CommonResult 统一响应结果
     *         - 成功时返回影响的记录数
     *         - 失败时返回失败状态
     */
    @Operation(summary = "根据ID删除后台资源")  //测试成功
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@PathVariable Long id) {
        // 调用服务层删除指定ID的资源
        int count = resourceService.delete(id);

        // 清空动态权限缓存，确保删除的资源权限配置立即失效
        dynamicSecurityMetadataSource.clearDataSource();

        // 根据影响记录数判断删除是否成功
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 分页模糊查询后台资源列表
     * 支持多条件组合查询，包括分类、名称关键字、URL关键字等
     *
     * @param categoryId 资源分类ID，可选参数，用于按分类筛选
     * @param nameKeyword 资源名称关键字，可选参数，用于模糊匹配资源名称
     * @param urlKeyword URL关键字，可选参数，用于模糊匹配资源URL
     * @param pageSize 每页显示记录数，默认为5条
     * @param pageNum 页码，默认为第1页
     * @return CommonResult<CommonPage<UmsResource>> 包含分页资源列表的统一响应结果
     */
    @Operation(summary = "分页模糊查询后台资源")  //测试成功
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<UmsResource>> list(@RequestParam(required = false) Long categoryId,
                                                      @RequestParam(required = false) String nameKeyword,
                                                      @RequestParam(required = false) String urlKeyword,
                                                      @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        // 调用服务层进行多条件分页查询
        List<UmsResource> resourceList = resourceService.list(categoryId, nameKeyword, urlKeyword, pageSize, pageNum);

        // 将查询结果转换为通用分页对象并返回成功结果
        return CommonResult.success(CommonPage.restPage(resourceList));
    }

    /**
     * 查询所有后台资源
     * 不分页，返回系统中的全部资源列表
     * 通常用于下拉选择框、权限配置等场景
     *
     * @return CommonResult<List<UmsResource>> 包含所有资源的统一响应结果
     */
    @Operation(summary = "查询所有后台资源")  //测试成功
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UmsResource>> listAll() {
        // 调用服务层查询所有资源
        List<UmsResource> resourceList = resourceService.listAll();

        // 返回包含全部资源列表的成功结果
        return CommonResult.success(resourceList);
    }
}