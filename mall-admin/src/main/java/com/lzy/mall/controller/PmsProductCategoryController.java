package com.lzy.mall.controller;

import com.lzy.mall.common.api.CommonPage;
import com.lzy.mall.common.api.CommonResult;
import com.lzy.mall.dto.PmsProductCategoryParam;
import com.lzy.mall.dto.PmsProductCategoryWithChildrenItem;
import com.lzy.mall.model.PmsProductCategory;
import com.lzy.mall.service.PmsProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类管理Controller
 * 提供商品分类的增删改查等RESTful API接口
 * 包括分类的创建、修改、查询、删除以及分类显示状态管理等功能
 */

@Controller
@Tag(name = "PmsProductCategoryController", description = "商品分类管理")
@RequestMapping("/productCategory")
public class PmsProductCategoryController {
    @Autowired
    private PmsProductCategoryService productCategoryService; // 商品分类服务

    /**
     * 创建新的商品分类
     * @param productCategoryParam 商品分类参数，包含分类的详细信息
     * @return 返回操作结果，包含创建成功的记录数
     */
    @Operation(summary = "添加商品分类")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult create(@Validated @RequestBody PmsProductCategoryParam productCategoryParam) {
        int count = productCategoryService.create(productCategoryParam);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 修改指定ID的商品分类信息
     * @param id 需要修改的商品分类ID
     * @param productCategoryParam 修改后的分类信息
     * @return 返回操作结果，包含更新成功的记录数
     */
    @Operation(summary = "修改商品分类")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult update(@PathVariable Long id,
                         @Validated
                         @RequestBody PmsProductCategoryParam productCategoryParam) {
        int count = productCategoryService.update(id, productCategoryParam);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 分页查询指定父分类下的商品分类列表
     * @param parentId 父分类ID，0表示一级分类
     * @param pageSize 每页显示数量，默认5条
     * @param pageNum 当前页码，默认第1页
     * @return 返回分页后的商品分类列表
     */
    @Operation(summary = "分页查询商品分类")
    @RequestMapping(value = "/list/{parentId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<PmsProductCategory>> getList(
            @PathVariable Long parentId,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<PmsProductCategory> productCategoryList = productCategoryService.getList(parentId, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(productCategoryList));
    }

    /**
     * 根据ID获取商品分类详情
     * @param id 商品分类ID
     * @return 返回指定ID的商品分类信息
     */
    @Operation(summary = "根据id获取商品分类")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<PmsProductCategory> getItem(@PathVariable Long id) {
        PmsProductCategory productCategory = productCategoryService.getItem(id);
        return CommonResult.success(productCategory);
    }

    /**
     * 删除指定ID的商品分类
     * @param id 要删除的商品分类ID
     * @return 返回操作结果，包含删除成功的记录数
     */
    @Operation(summary = "删除商品分类")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@PathVariable Long id) {
        int count = productCategoryService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 批量修改分类在导航栏的显示状态
     * @param ids 需要修改的分类ID列表
     * @param navStatus 导航栏显示状态：0->不显示；1->显示
     * @return 返回操作结果，包含更新成功的记录数
     */
    @Operation(summary = "修改导航栏显示状态")
    @RequestMapping(value = "/update/navStatus", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateNavStatus(
            @RequestParam("ids") List<Long> ids, 
            @RequestParam("navStatus") Integer navStatus) {
        int count = productCategoryService.updateNavStatus(ids, navStatus);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 批量修改分类的显示状态
     * @param ids 需要修改的分类ID列表
     * @param showStatus 显示状态：0->不显示；1->显示
     * @return 返回操作结果，包含更新成功的记录数
     */
    @Operation(summary = "修改显示状态")
    @RequestMapping(value = "/update/showStatus", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateShowStatus(
            @RequestParam("ids") List<Long> ids, 
            @RequestParam("showStatus") Integer showStatus) {
        int count = productCategoryService.updateShowStatus(ids, showStatus);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 查询所有一级分类及其子分类（树形结构）
     * 用于前端展示分类的层级关系
     * @return 返回包含层级关系的分类列表
     */
    @Operation(summary = "查询所有一级分类及子分类")
    @RequestMapping(value = "/list/withChildren", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PmsProductCategoryWithChildrenItem>> listWithChildren() {
        List<PmsProductCategoryWithChildrenItem> list = productCategoryService.listWithChildren();
        return CommonResult.success(list);
    }
}
