package com.lzy.mall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 订单发货参数
 */
@Getter
@Setter
public class OmsOrderDeliveryParam {
    @Schema(description = "订单id")
    private Long orderId;
    @Schema(description = "物流公司")
    private String deliveryCompany;
    @Schema(description = "物流单号")
    private String deliverySn;
}
