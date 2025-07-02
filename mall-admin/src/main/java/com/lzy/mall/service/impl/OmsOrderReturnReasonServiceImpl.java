package com.lzy.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.lzy.mall.mapper.OmsOrderReturnReasonMapper;
import com.lzy.mall.model.OmsOrderReturnReason;
import com.lzy.mall.model.OmsOrderReturnReasonExample;
import com.lzy.mall.service.OmsOrderReturnReasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 订单原因管理Service实现类
 * 实现了订单退货原因的新增、修改、删除、查询和状态更新等功能
 */
@Service
public class OmsOrderReturnReasonServiceImpl implements OmsOrderReturnReasonService {
    @Autowired
    private OmsOrderReturnReasonMapper returnReasonMapper;  // 订单退货原因Mapper
    @Override
    public int create(OmsOrderReturnReason returnReason) {
        // 设置创建时间为当前时间
        returnReason.setCreateTime(new Date());
        // 插入新的退货原因记录
        return returnReasonMapper.insert(returnReason);
    }

    @Override
    public int update(Long id, OmsOrderReturnReason returnReason) {
        // 设置要更新的退货原因ID
        returnReason.setId(id);
        // 根据主键更新退货原因信息
        return returnReasonMapper.updateByPrimaryKey(returnReason);
    }

    @Override
    public int delete(List<Long> ids) {
        // 创建查询条件：ID在指定列表中的记录
        OmsOrderReturnReasonExample example = new OmsOrderReturnReasonExample();
        example.createCriteria().andIdIn(ids);  // 设置ID列表条件
        
        // 执行批量删除操作，返回影响记录数
        return returnReasonMapper.deleteByExample(example);
    }

    @Override
    public List<OmsOrderReturnReason> list(Integer pageSize, Integer pageNum) {
        // 使用PageHelper进行分页查询
        PageHelper.startPage(pageNum, pageSize);
        
        // 创建查询条件
        OmsOrderReturnReasonExample example = new OmsOrderReturnReasonExample();
        // 设置排序规则：按sort字段降序排列
        example.setOrderByClause("sort desc");
        
        // 执行查询并返回结果
        return returnReasonMapper.selectByExample(example);
    }

    @Override
    public int updateStatus(List<Long> ids, Integer status) {
        // 检查状态值是否有效（0-禁用，1-启用）
        if (!status.equals(0) && !status.equals(1)) {
            return 0;  // 状态值无效，返回0表示更新失败
        }
        
        // 创建更新对象，设置要更新的状态
        OmsOrderReturnReason record = new OmsOrderReturnReason();
        record.setStatus(status);  // 设置新的状态值
        
        // 创建查询条件：ID在指定列表中的记录
        OmsOrderReturnReasonExample example = new OmsOrderReturnReasonExample();
        example.createCriteria().andIdIn(ids);  // 设置ID列表条件
        
        // 执行批量更新操作，返回影响记录数
        return returnReasonMapper.updateByExampleSelective(record, example);
    }

    @Override
    public OmsOrderReturnReason getItem(Long id) {
        // 根据主键ID查询退货原因详情
        return returnReasonMapper.selectByPrimaryKey(id);
    }
}
