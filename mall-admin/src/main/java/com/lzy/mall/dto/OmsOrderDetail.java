package com.lzy.mall.dto;

import com.lzy.mall.model.OmsOrder;
import com.lzy.mall.model.OmsOrderItem;
import com.lzy.mall.model.OmsOrderOperateHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 订单详情信息
 */
public class OmsOrderDetail extends OmsOrder {
    @Getter
    @Setter
    @Schema(description = "订单商品列表")
    private List<OmsOrderItem> orderItemList;
    @Getter
    @Setter
    @Schema(description = "订单操作记录列表")
    private List<OmsOrderOperateHistory> historyList;
}
