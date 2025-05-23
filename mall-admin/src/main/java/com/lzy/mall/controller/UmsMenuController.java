package com.lzy.mall.controller;

import com.lzy.mall.common.api.CommonPage;
import com.lzy.mall.common.api.CommonResult;
import com.lzy.mall.dto.UmsMenuNode;
import com.lzy.mall.model.UmsMenu;
import com.lzy.mall.service.UmsMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台菜单管理控制器。
 * <p>
 * 该类是Spring MVC的控制器层组件，负责处理所有与后台菜单相关的HTTP请求。
 * 它通过调用 `UmsMenuService` 接口中定义的业务逻辑方法，
 * 实现对后台菜单的增、删、改、查以及特殊查询（如树形结构查询）等功能。
 * 同时，使用了OpenAPI 3.0注解 (`@Operation`, `@Tag`) 为API接口生成详细的文档。
 */
@Controller // 标记这个类是一个Spring MVC控制器，能够处理HTTP请求
@Tag(name = "UmsMenuController", description = "后台菜单管理") // 为Swagger/OpenAPI文档定义API组名称和描述
@RequestMapping("/menu") // 定义该控制器下所有请求的父路径，即所有菜单相关的API都将以"/menu"开头
public class UmsMenuController {

    // 自动注入 UmsMenuService 接口的实现类。
    // Spring容器会在启动时找到并提供 UmsMenuService 的具体实现，
    // 以便控制器可以调用服务层的方法来处理业务逻辑。
    @Autowired
    private UmsMenuService menuService;

    /**
     * 添加后台菜单。
     * <p>
     * 此方法处理创建新后台菜单的POST请求。
     *
     * @param umsMenu 从请求体中解析的菜单实体对象，包含了新菜单的详细信息。
     * @return CommonResult 封装的操作结果，成功则包含影响的行数，失败则返回错误信息。
     */
    //测试成功
    @Operation(summary = "添加后台菜单") // Swagger/OpenAPI注解，用于API文档描述，说明该接口的作用
    @RequestMapping(value = "/create", method = RequestMethod.POST) // 映射到POST请求的"/menu/create"路径
    @ResponseBody // 表示方法返回的对象会被直接序列化为HTTP响应体（通常是JSON格式）
    public CommonResult create(@RequestBody UmsMenu umsMenu) {
        // 调用菜单服务层的create方法，执行菜单的插入操作。
        // create方法通常会返回数据库操作影响的行数。
        int count = menuService.create(umsMenu);
        // 根据操作影响的行数判断操作是否成功。
        if (count > 0) {
            // 如果影响行数大于0，表示菜单添加成功，返回成功响应，并附带影响行数。
            return CommonResult.success(count);
        } else {
            // 如果影响行数小于等于0，表示菜单添加失败，返回失败响应。
            return CommonResult.failed();
        }
    }

    /**
     * 修改后台菜单。
     * <p>
     * 此方法处理更新现有后台菜单的POST请求。
     *
     * @param id      路径变量，要修改的菜单的ID。
     * @param umsMenu 从请求体中解析的菜单实体对象，包含了更新后的菜单信息。
     * @return CommonResult 封装的操作结果，成功则包含影响的行数，失败则返回错误信息。
     */
    //测试成功
    @Operation(summary = "修改后台菜单") // Swagger/OpenAPI注解，用于API文档描述
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST) // 映射到POST请求的"/menu/update/{id}"路径
    @ResponseBody // 表示方法返回的对象会被直接序列化为HTTP响应体
    public CommonResult update(@PathVariable Long id, // 从URL路径中获取菜单ID
                               @RequestBody UmsMenu umsMenu) { // 从请求体中获取更新后的菜单数据
        // 调用菜单服务层的update方法，执行菜单的更新操作。
        // update方法通常会返回数据库操作影响的行数。
        int count = menuService.update(id, umsMenu);
        // 根据操作影响的行数判断操作是否成功。
        if (count > 0) {
            // 如果影响行数大于0，表示菜单更新成功，返回成功响应。
            return CommonResult.success(count);
        } else {
            // 如果影响行数小于等于0，表示菜单更新失败，返回失败响应。
            return CommonResult.failed();
        }
    }

    /**
     * 根据ID获取菜单详情。
     * <p>
     * 此方法处理根据ID查询单个后台菜单详细信息的GET请求。
     *
     * @param id 路径变量，要查询的菜单的ID。
     * @return CommonResult 封装的查询结果，成功则包含查询到的菜单对象，失败则返回错误信息。
     */
    //测试成功
    @Operation(summary = "根据ID获取菜单详情") // Swagger/OpenAPI注解，用于API文档描述
    @RequestMapping(value = "/{id}", method = RequestMethod.GET) // 映射到GET请求的"/menu/{id}"路径
    @ResponseBody // 表示方法返回的对象会被直接序列化为HTTP响应体
    public CommonResult<UmsMenu> getItem(@PathVariable Long id) { // 从URL路径中获取菜单ID
        // 调用菜单服务层的getItem方法，根据ID查询菜单详情。
        // 此方法会返回一个UmsMenu对象，如果找不到则可能返回null。
        UmsMenu umsMenu = menuService.getItem(id);
        // 将查询到的菜单对象封装到CommonResult的成功响应中并返回。
        // 即使查询结果为null，CommonResult.success(null) 也会表示操作成功。
        return CommonResult.success(umsMenu);
    }

    /**
     * 根据ID删除后台菜单。
     * <p>
     * 此方法处理删除指定后台菜单的POST请求。
     *
     * @param id 路径变量，要删除的菜单的ID。
     * @return CommonResult 封装的操作结果，成功则包含影响的行数，失败则返回错误信息。
     */
    //测试成功
    @Operation(summary = "根据ID删除后台菜单") // Swagger/OpenAPI注解，用于API文档描述
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST) // 映射到POST请求的"/menu/delete/{id}"路径
    @ResponseBody // 表示方法返回的对象会被直接序列化为HTTP响应体
    public CommonResult delete(@PathVariable Long id) { // 从URL路径中获取菜单ID
        // 调用菜单服务层的delete方法，执行菜单的删除操作。
        // delete方法通常会返回数据库操作影响的行数。
        int count = menuService.delete(id);
        // 根据操作影响的行数判断操作是否成功。
        if (count > 0) {
            // 如果影响行数大于0，表示菜单删除成功，返回成功响应。
            return CommonResult.success(count);
        } else {
            // 如果影响行数小于等于0，表示菜单删除失败，返回失败响应。
            return CommonResult.failed();
        }
    }

    /**
     * 分页查询后台菜单列表。
     * <p>
     * 此方法处理根据父级菜单ID进行过滤，并支持分页的GET请求。
     *
     * @param parentId 路径变量，父级菜单的ID。用于查询某个父级菜单下的所有子菜单。
     * @param pageSize 查询结果的每页大小，默认为5。
     * @param pageNum  查询结果的当前页码，默认为1。
     * @return CommonResult 封装的分页查询结果，成功则包含 `CommonPage` 对象，其中包含菜单列表和分页信息。
     */
    //测试成功
    @Operation(summary = "分页查询后台菜单") // Swagger/OpenAPI注解，用于API文档描述
    @RequestMapping(value = "/list/{parentId}", method = RequestMethod.GET) // 映射到GET请求的"/menu/list/{parentId}"路径
    @ResponseBody // 表示方法返回的对象会被直接序列化为HTTP响应体
    public CommonResult<CommonPage<UmsMenu>> list(@PathVariable Long parentId, // 从URL路径中获取父级菜单ID
                                                  @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize, // 从请求参数中获取每页大小，默认值为5
                                                  @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) { // 从请求参数中获取当前页码，默认值为1
        // 调用菜单服务层的list方法，执行带父ID和分页参数的菜单查询。
        // 此方法会返回当前页的菜单列表。
        List<UmsMenu> menuList = menuService.list(parentId, pageSize, pageNum);
        // 将查询到的菜单列表封装到CommonPage对象中，CommonPage会自动计算总页数、总条数等分页信息。
        // 然后将CommonPage对象封装到CommonResult的成功响应中并返回。
        return CommonResult.success(CommonPage.restPage(menuList));
    }

    /**
     * 以树形结构返回所有菜单列表。
     * <p>
     * 此方法处理获取所有后台菜单的树形结构表示的GET请求。
     * 这种结构通常用于前端展示菜单的层级关系（如多级导航栏）。
     *
     * @return CommonResult 封装的查询结果，成功则包含 `UmsMenuNode` 对象的树形列表。
     */
    //测试成功
    @Operation(summary = "树形结构返回所有菜单列表") // Swagger/OpenAPI注解，用于API文档描述
    @RequestMapping(value = "/treeList", method = RequestMethod.GET) // 映射到GET请求的"/menu/treeList"路径
    @ResponseBody // 表示方法返回的对象会被直接序列化为HTTP响应体
    public CommonResult<List<UmsMenuNode>> treeList() {
        // 调用菜单服务层的treeList方法，获取所有菜单的树形结构表示。
        // 此方法会返回一个List<UmsMenuNode>，其中UmsMenuNode包含子菜单列表。
        List<UmsMenuNode> list = menuService.treeList();
        // 将获取到的树形菜单列表封装到CommonResult的成功响应中并返回。
        return CommonResult.success(list);
    }

    /**
     * 修改菜单显示状态。
     * <p>
     * 此方法处理更新指定菜单的隐藏（显示/隐藏）状态的POST请求。
     *
     * @param id     路径变量，要修改状态的菜单的ID。
     * @param hidden 请求参数，表示菜单的新显示状态 (`0` 为显示，`1` 为隐藏)。
     * @return CommonResult 封装的操作结果，成功则包含影响的行数，失败则返回错误信息。
     */
    //测试成功
    @Operation(summary = "修改菜单显示状态") // Swagger/OpenAPI注解，用于API文档描述
    @RequestMapping(value = "/updateHidden/{id}", method = RequestMethod.POST) // 映射到POST请求的"/menu/updateHidden/{id}"路径
    @ResponseBody // 表示方法返回的对象会被直接序列化为HTTP响应体
    public CommonResult updateHidden(@PathVariable Long id, // 从URL路径中获取菜单ID
                                     @RequestParam("hidden") Integer hidden) { // 从请求参数中获取新的隐藏状态
        // 调用菜单服务层的updateHidden方法，执行菜单显示状态的更新操作。
        // 此方法会返回数据库操作影响的行数。
        int count = menuService.updateHidden(id, hidden);
        // 根据操作影响的行数判断操作是否成功。
        if (count > 0) {
            // 如果影响行数大于0，表示状态更新成功，返回成功响应。
            return CommonResult.success(count);
        } else {
            // 如果影响行数小于等于0，表示状态更新失败，返回失败响应。
            return CommonResult.failed();
        }
    }
}
