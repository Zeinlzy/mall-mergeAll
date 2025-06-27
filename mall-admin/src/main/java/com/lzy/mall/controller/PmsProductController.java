package com.lzy.mall.controller;

import com.lzy.mall.common.api.CommonPage;
import com.lzy.mall.common.api.CommonResult;
import com.lzy.mall.dto.PmsProductParam;
import com.lzy.mall.dto.PmsProductQueryParam;
import com.lzy.mall.dto.PmsProductResult;
import com.lzy.mall.model.PmsProduct;
import com.lzy.mall.service.PmsProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理控制器
 * 处理商品相关的所有HTTP请求，包括商品的CRUD操作以及状态管理
 */
@Controller
@Tag(name = "PmsProductController", description = "商品管理")
@RequestMapping("/product")
public class PmsProductController {
        @Autowired
    private PmsProductService productService; // 商品服务接口

    /**
     * 创建新商品
     * @param productParam 商品参数，包含商品的基本信息、规格、属性等
     * @return 创建结果，包含影响的行数
     */
    @Operation(summary = "创建商品")  //测试通过
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult create(@RequestBody PmsProductParam productParam) {
        int count = productService.create(productParam);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 获取商品编辑信息
     * @param id 商品ID
     * @return 包含商品完整信息的响应结果
     */
    @Operation(summary = "根据商品id获取商品编辑信息")  //测试通过
    @RequestMapping(value = "/updateInfo/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<PmsProductResult> getUpdateInfo(@PathVariable Long id) {
        PmsProductResult productResult = productService.getUpdateInfo(id);
        return CommonResult.success(productResult);
    }

    /**
     * 更新商品信息
     * @param id 商品ID
     * @param productParam 更新后的商品信息
     * @return 更新结果，包含影响的行数
     */
    @Operation(summary = "更新商品")  //测试通过
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult update(@PathVariable Long id, @RequestBody PmsProductParam productParam) {
        int count = productService.update(id, productParam);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 分页查询商品列表
     * @param productQueryParam 商品查询参数
     * @param pageSize 每页显示条数，默认5条
     * @param pageNum 当前页码，默认第1页
     * @return 分页商品列表
     */
    @Operation(summary = "查询商品") //测试成功
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<PmsProduct>> getList(PmsProductQueryParam productQueryParam,
                                                      @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<PmsProduct> productList = productService.list(productQueryParam, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(productList));
    }

    /**
     * 根据关键词模糊查询商品
     * @param keyword 搜索关键词，可匹配商品名称或货号
     * @return 匹配的商品列表
     */
    @Operation(summary = "根据商品名称或货号模糊查询")  //测试通过
    @RequestMapping(value = "/simpleList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PmsProduct>> getList(String keyword) {
        List<PmsProduct> productList = productService.list(keyword);
        return CommonResult.success(productList);
    }

    /**
     * 批量更新商品审核状态
     * @param ids 商品ID列表
     * @param verifyStatus 审核状态
     * @param detail 审核详情
     * @return 更新结果
     */
    @Operation(summary = "批量修改审核状态") //测试通过
    @RequestMapping(value = "/update/verifyStatus", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateVerifyStatus(@RequestParam("ids") List<Long> ids,
                                         @RequestParam("verifyStatus") Integer verifyStatus,
                                         @RequestParam("detail") String detail) {
        int count = productService.updateVerifyStatus(ids, verifyStatus, detail);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 批量上下架商品
     * @param ids 商品ID列表
     * @param publishStatus 上架状态：0->下架；1->上架
     * @return 更新结果
     */
    @Operation(summary = "批量上下架商品") //测试成功
    @RequestMapping(value = "/update/publishStatus", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updatePublishStatus(@RequestParam("ids") List<Long> ids,
                                          @RequestParam("publishStatus") Integer publishStatus) {
        int count = productService.updatePublishStatus(ids, publishStatus);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 批量设置商品推荐状态
     * @param ids 商品ID列表
     * @param recommendStatus 推荐状态：0->不推荐；1->推荐
     * @return 更新结果
     */
    @Operation(summary = "批量推荐商品")  //测试通过
    @RequestMapping(value = "/update/recommendStatus", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateRecommendStatus(@RequestParam("ids") List<Long> ids,
                                            @RequestParam("recommendStatus") Integer recommendStatus) {
        int count = productService.updateRecommendStatus(ids, recommendStatus);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 批量设置商品新品状态
     * @param ids 商品ID列表
     * @param newStatus 新品状态：0->不是新品；1->新品
     * @return 更新结果
     */
    @Operation(summary = "批量设为新品")  //测试通过
    @RequestMapping(value = "/update/newStatus", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateNewStatus(@RequestParam("ids") List<Long> ids,
                                      @RequestParam("newStatus") Integer newStatus) {
        int count = productService.updateNewStatus(ids, newStatus);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 批量修改商品删除状态
     * @param ids 商品ID列表
     * @param deleteStatus 删除状态：0->未删除；1->已删除
     * @return 更新结果
     */
    @Operation(summary = "批量修改删除状态")  //测试通过
    @RequestMapping(value = "/update/deleteStatus", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateDeleteStatus(@RequestParam("ids") List<Long> ids,
                                         @RequestParam("deleteStatus") Integer deleteStatus) {
        int count = productService.updateDeleteStatus(ids, deleteStatus);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }
}
