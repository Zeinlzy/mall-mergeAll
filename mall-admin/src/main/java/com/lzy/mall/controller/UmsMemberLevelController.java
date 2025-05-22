package com.lzy.mall.controller;

import com.lzy.mall.common.api.CommonResult;
import com.lzy.mall.model.UmsMemberLevel;
import com.lzy.mall.service.UmsMemberLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 会员等级管理Controller
 */
@Controller
@Tag(name = "UmsMemberLevelController", description = "会员等级管理")
@RequestMapping("/memberLevel")
public class UmsMemberLevelController {

    // 注入会员等级服务，Spring会自动找到并提供一个UmsMemberLevelService的实例
    @Autowired
    private UmsMemberLevelService memberLevelService;

    /**
     * 查询会员等级列表。
     * <p>
     * 此方法根据传入的 `defaultStatus` 参数过滤会员等级列表。
     * `defaultStatus` 为 `1` 时，表示查询系统默认的会员等级（例如：新用户注册后的初始等级）；
     * `defaultStatus` 为 `0` 时，表示查询非默认的会员等级（通常是需要达到一定条件才能升级的等级，如黄金、白金、钻石会员等）。
     *
     * @param defaultStatus 默认状态标识，Integer类型。
     *                      `1`：查询默认等级。
     *                      `0`：查询非默认等级。
     * @return 包含会员等级列表的通用结果对象 `CommonResult<List<UmsMemberLevel>>`。
     */
    //测试成功
    @Operation(summary = "查询所有会员等级") // Swagger/OpenAPI 注解，用于API文档描述，说明该接口的作用
    @RequestMapping(value = "/list", method = RequestMethod.GET) // 将此方法映射到 HTTP GET 请求的 "/memberLevel/list" 路径
    @ResponseBody // 表示方法返回的对象会被直接序列化为HTTP响应体（通常是JSON格式）
    public CommonResult<List<UmsMemberLevel>> list(@RequestParam("defaultStatus") Integer defaultStatus) {
        // 调用会员等级服务（memberLevelService）的 `list` 方法，
        // 并传入从请求参数中获取到的 `defaultStatus` 值。
        // `memberLevelService` 会根据 `defaultStatus` 到数据库中查询相应的会员等级数据。
        List<UmsMemberLevel> memberLevelList = memberLevelService.list(defaultStatus);

        // 将查询到的会员等级列表 `memberLevelList` 封装到 `CommonResult.success()` 方法中。
        // `CommonResult.success()` 会创建一个表示操作成功的响应对象，包含查询到的数据。
        // 最后，将这个响应对象返回给客户端。
        return CommonResult.success(memberLevelList);
    }
}
