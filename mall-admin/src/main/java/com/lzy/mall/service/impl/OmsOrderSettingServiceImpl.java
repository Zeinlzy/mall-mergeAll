package com.lzy.mall.service.impl;

import com.lzy.mall.mapper.OmsOrderSettingMapper;
import com.lzy.mall.model.OmsOrderSetting;
import com.lzy.mall.service.OmsOrderSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 订单设置管理Service实现类
 * 实现了订单设置信息的查询和更新功能
 */
@Service
public class OmsOrderSettingServiceImpl implements OmsOrderSettingService {
    @Autowired
    private OmsOrderSettingMapper orderSettingMapper;  // 订单设置信息Mapper

    @Override
    public OmsOrderSetting getItem(Long id) {
        // 根据主键ID查询订单设置信息
        return orderSettingMapper.selectByPrimaryKey(id);
    }

    @Override
    public int update(Long id, OmsOrderSetting orderSetting) {
        // 设置要更新的订单设置ID
        orderSetting.setId(id);
        // 根据主键更新订单设置信息
        return orderSettingMapper.updateByPrimaryKey(orderSetting);
    }
}
