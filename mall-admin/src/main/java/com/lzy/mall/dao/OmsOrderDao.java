package com.lzy.mall.dao;

import com.lzy.mall.dto.OmsOrderDeliveryParam;
import com.lzy.mall.dto.OmsOrderDetail;
import com.lzy.mall.dto.OmsOrderQueryParam;
import com.lzy.mall.model.OmsOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单查询自定义Dao
 */
public interface OmsOrderDao {
    /**
     * 条件查询订单
     */
    List<OmsOrder> getList(@Param("queryParam") OmsOrderQueryParam queryParam);

    /**
     * 批量发货
     */
    int delivery(@Param("list") List<OmsOrderDeliveryParam> deliveryParamList);

    /**
     * 获取订单详情
     */
    OmsOrderDetail getDetail(@Param("id") Long id);
}
