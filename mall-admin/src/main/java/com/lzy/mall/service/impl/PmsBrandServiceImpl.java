package com.lzy.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.lzy.mall.dto.PmsBrandParam;
import com.lzy.mall.mapper.PmsBrandMapper;
import com.lzy.mall.mapper.PmsProductMapper;
import com.lzy.mall.model.PmsBrand;
import com.lzy.mall.model.PmsBrandExample;
import com.lzy.mall.model.PmsProduct;
import com.lzy.mall.model.PmsProductExample;
import com.lzy.mall.service.PmsBrandService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 商品品牌管理Service实现类
 * 提供商品品牌的CRUD、分页查询、状态更新等功能
 */
@Service
public class PmsBrandServiceImpl implements PmsBrandService {
    // 品牌数据访问对象
    @Autowired
    private PmsBrandMapper brandMapper;
    
    // 商品数据访问对象，用于级联更新商品信息
    @Autowired
    private PmsProductMapper productMapper;

    /**
     * 获取所有品牌列表
     * 
     * @return 返回所有品牌信息的列表
     */
    @Override
    public List<PmsBrand> listAllBrand() {
        // 创建空的查询条件，查询所有品牌
        return brandMapper.selectByExample(new PmsBrandExample());
    }

    /**
     * 创建新品牌
     * 1. 将参数转换为品牌实体
     * 2. 处理首字母（如未提供则自动生成）
     * 3. 保存品牌信息到数据库
     * 
     * @param pmsBrandParam 品牌参数对象，包含品牌相关信息
     * @return 返回插入的记录数
     */
    @Override
    public int createBrand(PmsBrandParam pmsBrandParam) {
        // 1. 创建品牌对象并复制属性
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtils.copyProperties(pmsBrandParam, pmsBrand);
        
        // 2. 处理首字母：如果未提供，则取品牌名称的第一个字符作为首字母
        if (StrUtil.isEmpty(pmsBrand.getFirstLetter())) {
            pmsBrand.setFirstLetter(pmsBrand.getName().substring(0, 1).toUpperCase());
        }
        
        // 3. 保存品牌信息到数据库
        return brandMapper.insertSelective(pmsBrand);
    }

    /**
     * 更新品牌信息
     * 1. 更新品牌基本信息
     * 2. 处理首字母（如未提供则自动生成）
     * 3. 级联更新关联商品的品牌名称
     * 4. 更新品牌信息到数据库
     * 
     * @param id 品牌ID
     * @param pmsBrandParam 品牌参数对象
     * @return 返回更新的记录数
     */
    @Override
    public int updateBrand(Long id, PmsBrandParam pmsBrandParam) {
        // 1. 创建品牌对象并设置ID
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtils.copyProperties(pmsBrandParam, pmsBrand);
        pmsBrand.setId(id);
        
        // 2. 处理首字母：如果未提供，则取品牌名称的第一个字符作为首字母
        if (StrUtil.isEmpty(pmsBrand.getFirstLetter())) {
            pmsBrand.setFirstLetter(pmsBrand.getName().substring(0, 1).toUpperCase());
        }
        
        // 3. 级联更新：更新关联商品的品牌名称
        if (pmsBrandParam.getName() != null) {
            PmsProduct product = new PmsProduct();
            product.setBrandName(pmsBrand.getName());
            PmsProductExample example = new PmsProductExample();
            example.createCriteria().andBrandIdEqualTo(id);
            productMapper.updateByExampleSelective(product, example);
        }
        
        // 4. 更新品牌信息到数据库
        return brandMapper.updateByPrimaryKeySelective(pmsBrand);
    }

    /**
     * 根据ID删除品牌
     * 注意：此操作会删除品牌，但不会级联删除关联的商品
     * 
     * @param id 品牌ID
     * @return 返回删除的记录数
     */
    @Override
    public int deleteBrand(Long id) {
        // 根据主键ID删除品牌记录
        // 注意：需要确保没有商品关联到此品牌，否则可能会导致数据不一致
        return brandMapper.deleteByPrimaryKey(id);
    }

    /**
     * 批量删除品牌
     * 注意：此操作会删除多个品牌，但不会级联删除关联的商品
     * 
     * @param ids 品牌ID列表
     * @return 返回删除的记录数
     */
    @Override
    public int deleteBrand(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        // 创建查询条件：ID在指定列表中
        PmsBrandExample pmsBrandExample = new PmsBrandExample();
        pmsBrandExample.createCriteria().andIdIn(ids);
        
        // 执行批量删除
        return brandMapper.deleteByExample(pmsBrandExample);
    }

    /**
     * 分页查询品牌列表
     * 1. 支持按品牌名称模糊查询
     * 2. 支持按显示状态筛选
     * 3. 结果按sort字段降序排序
     * 
     * @param keyword 品牌名称关键词，支持模糊查询
     * @param showStatus 显示状态：0->不显示；1->显示
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @return 返回分页后的品牌列表
     */
    @Override
    public List<PmsBrand> listBrand(String keyword, Integer showStatus, int pageNum, int pageSize) {
        // 1. 设置分页参数
        PageHelper.startPage(pageNum, pageSize);
        
        // 2. 创建查询条件
        PmsBrandExample pmsBrandExample = new PmsBrandExample();
        // 3. 设置排序规则：按sort字段降序
        pmsBrandExample.setOrderByClause("sort desc");
        
        // 4. 构建查询条件
        PmsBrandExample.Criteria criteria = pmsBrandExample.createCriteria();
        // 4.1 添加品牌名称模糊查询条件
        if (!StrUtil.isEmpty(keyword)) {
            criteria.andNameLike("%" + keyword + "%");
        }
        // 4.2 添加显示状态查询条件
        if (showStatus != null) {
            criteria.andShowStatusEqualTo(showStatus);
        }
        
        // 5. 执行查询并返回结果
        return brandMapper.selectByExample(pmsBrandExample);
    }

    /**
     * 根据ID获取品牌详情
     * 
     * @param id 品牌ID
     * @return 返回品牌详细信息，如果不存在则返回null
     */
    @Override
    public PmsBrand getBrand(Long id) {
        // 根据主键ID查询品牌信息
        return brandMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量更新品牌的显示状态
     * 
     * @param ids 需要更新的品牌ID列表
     * @param showStatus 显示状态：0->不显示；1->显示
     * @return 返回更新的记录数
     */
    @Override
    public int updateShowStatus(List<Long> ids, Integer showStatus) {
        if (CollectionUtils.isEmpty(ids) || showStatus == null) {
            return 0;
        }
        
        // 1. 创建要更新的字段对象
        PmsBrand pmsBrand = new PmsBrand();
        pmsBrand.setShowStatus(showStatus);
        
        // 2. 创建查询条件：ID在指定列表中
        PmsBrandExample pmsBrandExample = new PmsBrandExample();
        pmsBrandExample.createCriteria().andIdIn(ids);
        
        // 3. 执行批量更新并返回结果
        return brandMapper.updateByExampleSelective(pmsBrand, pmsBrandExample);
    }

    /**
     * 批量更新品牌的是否为品牌制造商状态
     * 
     * @param ids 需要更新的品牌ID列表
     * @param factoryStatus 是否为品牌制造商：0->不是；1->是
     * @return 返回更新的记录数
     */
    @Override
    public int updateFactoryStatus(List<Long> ids, Integer factoryStatus) {
        if (CollectionUtils.isEmpty(ids) || factoryStatus == null) {
            return 0;
        }
        
        // 1. 创建要更新的字段对象
        PmsBrand pmsBrand = new PmsBrand();
        pmsBrand.setFactoryStatus(factoryStatus);
        
        // 2. 创建查询条件：ID在指定列表中
        PmsBrandExample pmsBrandExample = new PmsBrandExample();
        pmsBrandExample.createCriteria().andIdIn(ids);
        
        // 3. 执行批量更新并返回结果
        return brandMapper.updateByExampleSelective(pmsBrand, pmsBrandExample);
    }
}
