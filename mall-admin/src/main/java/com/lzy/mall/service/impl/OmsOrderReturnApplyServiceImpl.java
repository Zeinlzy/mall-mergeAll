package com.lzy.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.lzy.mall.dao.OmsOrderReturnApplyDao;
import com.lzy.mall.dto.OmsOrderReturnApplyResult;
import com.lzy.mall.dto.OmsReturnApplyQueryParam;
import com.lzy.mall.dto.OmsUpdateStatusParam;
import com.lzy.mall.mapper.OmsOrderReturnApplyMapper;
import com.lzy.mall.model.OmsOrderReturnApply;
import com.lzy.mall.model.OmsOrderReturnApplyExample;
import com.lzy.mall.service.OmsOrderReturnApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 订单退货管理Service实现类
 * 实现了订单退货申请的查询、删除、状态更新和详情查看等功能
 */
@Service
public class OmsOrderReturnApplyServiceImpl implements OmsOrderReturnApplyService {
    @Autowired
    private OmsOrderReturnApplyDao returnApplyDao;  // 订单退货申请自定义Dao
    
    @Autowired
    private OmsOrderReturnApplyMapper returnApplyMapper;  // 订单退货申请基础Mapper
    @Override
    public List<OmsOrderReturnApply> list(OmsReturnApplyQueryParam queryParam, Integer pageSize, Integer pageNum) {
        // 使用PageHelper进行分页查询
        PageHelper.startPage(pageNum, pageSize);
        // 调用Dao层获取退货申请列表
        return returnApplyDao.getList(queryParam);
    }

    @Override
    public int delete(List<Long> ids) {
        // 创建查询条件：ID在指定列表中且状态为已拒绝(3)的退货申请
        OmsOrderReturnApplyExample example = new OmsOrderReturnApplyExample();
        example.createCriteria()
               .andIdIn(ids)  // 指定ID列表
               .andStatusEqualTo(3);  // 状态为已拒绝
        
        // 执行删除操作，返回影响记录数
        return returnApplyMapper.deleteByExample(example);
    }

    @Override
    public int updateStatus(Long id, OmsUpdateStatusParam statusParam) {
        // 获取要更新的状态
        Integer status = statusParam.getStatus();
        OmsOrderReturnApply returnApply = new OmsOrderReturnApply();
        
        if (status.equals(1)) {
            // 状态1：确认退货
            returnApply.setId(id);  // 设置退货申请ID
            returnApply.setStatus(1);  // 设置状态为已确认
            returnApply.setReturnAmount(statusParam.getReturnAmount());  // 设置退款金额
            returnApply.setCompanyAddressId(statusParam.getCompanyAddressId());  // 设置公司收货地址ID
            returnApply.setHandleTime(new Date());  // 设置处理时间
            returnApply.setHandleMan(statusParam.getHandleMan());  // 设置处理人
            returnApply.setHandleNote(statusParam.getHandleNote());  // 设置处理备注
        } else if (status.equals(2)) {
            // 状态2：完成退货
            returnApply.setId(id);  // 设置退货申请ID
            returnApply.setStatus(2);  // 设置状态为已完成
            returnApply.setReceiveTime(new Date());  // 设置收货时间
            returnApply.setReceiveMan(statusParam.getReceiveMan());  // 设置收货人
            returnApply.setReceiveNote(statusParam.getReceiveNote());  // 设置收货备注
        } else if (status.equals(3)) {
            // 状态3：拒绝退货
            returnApply.setId(id);  // 设置退货申请ID
            returnApply.setStatus(3);  // 设置状态为已拒绝
            returnApply.setHandleTime(new Date());  // 设置处理时间
            returnApply.setHandleMan(statusParam.getHandleMan());  // 设置处理人
            returnApply.setHandleNote(statusParam.getHandleNote());  // 设置处理备注
        } else {
            // 无效的状态值，返回0表示更新失败
            return 0;
        }
        
        // 根据主键选择性更新退货申请信息
        return returnApplyMapper.updateByPrimaryKeySelective(returnApply);
    }

    @Override
    public OmsOrderReturnApplyResult getItem(Long id) {
        // 根据ID查询退货申请详情
        return returnApplyDao.getDetail(id);
    }
}
