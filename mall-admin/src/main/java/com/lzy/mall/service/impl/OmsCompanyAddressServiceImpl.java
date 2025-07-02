package com.lzy.mall.service.impl;

import com.lzy.mall.mapper.OmsCompanyAddressMapper;
import com.lzy.mall.model.OmsCompanyAddress;
import com.lzy.mall.model.OmsCompanyAddressExample;
import com.lzy.mall.service.OmsCompanyAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 收货地址管理Service实现类
 */
@Service
public class OmsCompanyAddressServiceImpl implements OmsCompanyAddressService {
    @Autowired
    private OmsCompanyAddressMapper companyAddressMapper;
    @Override
    public List<OmsCompanyAddress> list() {
        return companyAddressMapper.selectByExample(new OmsCompanyAddressExample());
    }
}
