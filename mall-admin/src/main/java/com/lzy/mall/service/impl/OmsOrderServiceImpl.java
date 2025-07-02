package com.lzy.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.lzy.mall.dao.OmsOrderDao;
import com.lzy.mall.dao.OmsOrderOperateHistoryDao;
import com.lzy.mall.dto.*;
import com.lzy.mall.mapper.OmsOrderMapper;
import com.lzy.mall.mapper.OmsOrderOperateHistoryMapper;
import com.lzy.mall.model.OmsOrder;
import com.lzy.mall.model.OmsOrderExample;
import com.lzy.mall.model.OmsOrderOperateHistory;
import com.lzy.mall.service.OmsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单管理Service实现类
 * 实现了订单的查询、发货、关闭、删除、详情查看、修改收货人信息、修改费用信息、修改备注等功能
 */
@Service
public class OmsOrderServiceImpl implements OmsOrderService {
    @Autowired
    private OmsOrderMapper orderMapper;  // 订单基础Mapper
    
    @Autowired
    private OmsOrderDao orderDao;  // 订单自定义Dao
    
    @Autowired
    private OmsOrderOperateHistoryDao orderOperateHistoryDao;  // 订单操作历史Dao
    
    @Autowired
    private OmsOrderOperateHistoryMapper orderOperateHistoryMapper;  // 订单操作历史Mapper

    @Override
    public List<OmsOrder> list(OmsOrderQueryParam queryParam, Integer pageSize, Integer pageNum) {
        // 使用PageHelper进行分页查询
        PageHelper.startPage(pageNum, pageSize);
        // 调用Dao层获取订单列表
        return orderDao.getList(queryParam);
    }

    @Override
    public int delivery(List<OmsOrderDeliveryParam> deliveryParamList) {
        // 批量更新订单状态为已发货
        int count = orderDao.delivery(deliveryParamList);
        
        // 为每个发货的订单添加操作记录
        List<OmsOrderOperateHistory> operateHistoryList = deliveryParamList.stream()
                .map(omsOrderDeliveryParam -> {
                    OmsOrderOperateHistory history = new OmsOrderOperateHistory();
                    history.setOrderId(omsOrderDeliveryParam.getOrderId());  // 设置订单ID
                    history.setCreateTime(new Date());  // 设置操作时间
                    history.setOperateMan("后台管理员");  // 设置操作人
                    history.setOrderStatus(2);  // 设置订单状态为2(已发货)
                    history.setNote("完成发货");  // 设置操作备注
                    return history;
                }).collect(Collectors.toList());
                
        // 批量插入操作记录
        orderOperateHistoryDao.insertList(operateHistoryList);
        return count;  // 返回更新记录数
    }

    @Override
    public int close(List<Long> ids, String note) {
        // 创建订单更新对象，设置状态为4(已关闭)
        OmsOrder record = new OmsOrder();
        record.setStatus(4);
        
        // 创建查询条件：未删除且ID在指定列表中的订单
        OmsOrderExample example = new OmsOrderExample();
        example.createCriteria()
               .andDeleteStatusEqualTo(0)  // 未删除的订单
               .andIdIn(ids);  // 指定ID列表
               
        // 批量更新订单状态为已关闭
        int count = orderMapper.updateByExampleSelective(record, example);
        
        // 为每个关闭的订单生成操作记录
        List<OmsOrderOperateHistory> historyList = ids.stream().map(orderId -> {
            OmsOrderOperateHistory history = new OmsOrderOperateHistory();
            history.setOrderId(orderId);  // 设置订单ID
            history.setCreateTime(new Date());  // 设置操作时间
            history.setOperateMan("后台管理员");  // 设置操作人
            history.setOrderStatus(4);  // 设置订单状态为4(已关闭)
            history.setNote("订单关闭:" + note);  // 设置关闭原因
            return history;
        }).collect(Collectors.toList());
        
        // 批量插入操作记录
        orderOperateHistoryDao.insertList(historyList);
        return count;  // 返回更新记录数
    }

    @Override
    public int delete(List<Long> ids) {
        // 创建订单更新对象，设置删除状态为1(已删除)
        OmsOrder record = new OmsOrder();
        record.setDeleteStatus(1);
        
        // 创建查询条件：未删除且ID在指定列表中的订单
        OmsOrderExample example = new OmsOrderExample();
        example.createCriteria()
               .andDeleteStatusEqualTo(0)  // 未删除的订单
               .andIdIn(ids);  // 指定ID列表
               
        // 执行逻辑删除操作，返回影响记录数
        return orderMapper.updateByExampleSelective(record, example);
    }

    @Override
    public OmsOrderDetail detail(Long id) {
        // 根据订单ID查询订单详情
        return orderDao.getDetail(id);
    }

    @Override
    public int updateReceiverInfo(OmsReceiverInfoParam receiverInfoParam) {
        // 创建订单更新对象，设置新的收货人信息
        OmsOrder order = new OmsOrder();
        order.setId(receiverInfoParam.getOrderId());  // 设置订单ID
        order.setReceiverName(receiverInfoParam.getReceiverName());  // 收货人姓名
        order.setReceiverPhone(receiverInfoParam.getReceiverPhone());  // 收货人电话
        order.setReceiverPostCode(receiverInfoParam.getReceiverPostCode());  // 邮政编码
        order.setReceiverDetailAddress(receiverInfoParam.getReceiverDetailAddress());  // 详细地址
        order.setReceiverProvince(receiverInfoParam.getReceiverProvince());  // 省份
        order.setReceiverCity(receiverInfoParam.getReceiverCity());  // 城市
        order.setReceiverRegion(receiverInfoParam.getReceiverRegion());  // 区/县
        order.setModifyTime(new Date());  // 修改时间
        
        // 更新订单收货人信息
        int count = orderMapper.updateByPrimaryKeySelective(order);
        
        // 添加操作记录
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(receiverInfoParam.getOrderId());  // 设置订单ID
        history.setCreateTime(new Date());  // 设置操作时间
        history.setOperateMan("后台管理员");  // 设置操作人
        history.setOrderStatus(receiverInfoParam.getStatus());  // 设置订单状态
        history.setNote("修改收货人信息");  // 设置操作备注
        orderOperateHistoryMapper.insert(history);  // 插入操作记录
        
        return count;  // 返回更新记录数
    }

    @Override
    public int updateMoneyInfo(OmsMoneyInfoParam moneyInfoParam) {
        // 创建订单更新对象，设置费用相关信息
        OmsOrder order = new OmsOrder();
        order.setId(moneyInfoParam.getOrderId());  // 设置订单ID
        order.setFreightAmount(moneyInfoParam.getFreightAmount());  // 设置运费金额
        order.setDiscountAmount(moneyInfoParam.getDiscountAmount());  // 设置优惠金额
        order.setModifyTime(new Date());  // 设置修改时间
        
        // 更新订单费用信息
        int count = orderMapper.updateByPrimaryKeySelective(order);
        
        // 添加操作记录
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(moneyInfoParam.getOrderId());  // 设置订单ID
        history.setCreateTime(new Date());  // 设置操作时间
        history.setOperateMan("后台管理员");  // 设置操作人
        history.setOrderStatus(moneyInfoParam.getStatus());  // 设置订单状态
        history.setNote("修改费用信息");  // 设置操作备注
        orderOperateHistoryMapper.insert(history);  // 插入操作记录
        
        return count;  // 返回更新记录数
    }

    @Override
    public int updateNote(Long id, String note, Integer status) {
        // 创建订单更新对象，设置备注信息
        OmsOrder order = new OmsOrder();
        order.setId(id);  // 设置订单ID
        order.setNote(note);  // 设置备注内容
        order.setModifyTime(new Date());  // 设置修改时间
        
        // 更新订单备注信息
        int count = orderMapper.updateByPrimaryKeySelective(order);
        
        // 添加操作记录
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(id);  // 设置订单ID
        history.setCreateTime(new Date());  // 设置操作时间
        history.setOperateMan("后台管理员");  // 设置操作人
        history.setOrderStatus(status);  // 设置订单状态
        history.setNote("修改备注信息：" + note);  // 设置操作备注，包含修改后的备注内容
        orderOperateHistoryMapper.insert(history);  // 插入操作记录
        
        return count;  // 返回更新记录数
    }
}
