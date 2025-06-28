package com.lzy.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.lzy.mall.dao.PmsProductCategoryAttributeRelationDao;
import com.lzy.mall.dao.PmsProductCategoryDao;
import com.lzy.mall.dto.PmsProductCategoryParam;
import com.lzy.mall.dto.PmsProductCategoryWithChildrenItem;
import com.lzy.mall.mapper.PmsProductCategoryAttributeRelationMapper;
import com.lzy.mall.mapper.PmsProductCategoryMapper;
import com.lzy.mall.mapper.PmsProductMapper;
import com.lzy.mall.model.*;
import com.lzy.mall.service.PmsProductCategoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类服务实现类
 * 处理商品分类相关的业务逻辑
 */
@Service
public class PmsProductCategoryServiceImpl implements PmsProductCategoryService {
    @Autowired
    private PmsProductCategoryMapper productCategoryMapper; // 商品分类Mapper
    
    @Autowired
    private PmsProductMapper productMapper; // 商品Mapper
    
    @Autowired
    private PmsProductCategoryAttributeRelationDao productCategoryAttributeRelationDao; // 商品分类属性关系DAO
    
    @Autowired
    private PmsProductCategoryAttributeRelationMapper productCategoryAttributeRelationMapper; // 商品分类属性关系Mapper
    
    @Autowired
    private PmsProductCategoryDao productCategoryDao; // 商品分类DAO

    /**
     * 创建商品分类
     * 1. 创建新的商品分类记录
     * 2. 设置商品数量初始为0
     * 3. 设置分类层级
     * 4. 保存商品分类到数据库
     * 5. 如果有关联的属性ID列表，则创建分类与属性的关联关系
     * 
     * @param pmsProductCategoryParam 商品分类参数，包含分类的基本信息
     * @return 创建成功的记录数
     */
    @Override
    public int create(PmsProductCategoryParam pmsProductCategoryParam) {
        // 1. 创建新的商品分类对象
        PmsProductCategory productCategory = new PmsProductCategory();
        // 2. 设置商品数量初始为0
        productCategory.setProductCount(0);
        // 3. 将参数中的属性值复制到商品分类对象
        BeanUtils.copyProperties(pmsProductCategoryParam, productCategory);
        // 4. 设置分类的层级（一级、二级等）
        setCategoryLevel(productCategory);
        // 5. 将商品分类保存到数据库
        int count = productCategoryMapper.insertSelective(productCategory);
        // 6. 处理商品分类与属性的关联关系
        List<Long> productAttributeIdList = pmsProductCategoryParam.getProductAttributeIdList();
        if(!CollectionUtils.isEmpty(productAttributeIdList)){
            insertRelationList(productCategory.getId(), productAttributeIdList);
        }
        return count;
    }

    /**
     * 批量插入商品分类与筛选属性关系表
     * @param productCategoryId 商品分类id
     * @param productAttributeIdList 相关商品筛选属性id集合
     */
    /**
     * 批量插入商品分类与属性的关联关系
     * 1. 遍历属性ID列表，为每个属性创建关联关系对象
     * 2. 设置关联关系的基本信息
     * 3. 批量插入到数据库
     * 
     * @param productCategoryId 商品分类ID
     * @param productAttributeIdList 商品属性ID列表
     */
    private void insertRelationList(Long productCategoryId, List<Long> productAttributeIdList) {
        // 1. 创建关联关系列表
        List<PmsProductCategoryAttributeRelation> relationList = new ArrayList<>();
        
        // 2. 遍历属性ID列表
        for (Long productAttrId : productAttributeIdList) {
            // 2.1 创建关联关系对象
            PmsProductCategoryAttributeRelation relation = new PmsProductCategoryAttributeRelation();
            // 2.2 设置属性ID
            relation.setProductAttributeId(productAttrId);
            // 2.3 设置分类ID
            relation.setProductCategoryId(productCategoryId);
            // 2.4 添加到列表
            relationList.add(relation);
        }
        
        // 3. 批量插入关联关系到数据库
        productCategoryAttributeRelationDao.insertList(relationList);
    }

    /**
     * 更新商品分类信息
     * @param id 商品分类ID
     * @param pmsProductCategoryParam 商品分类参数
     * @return 更新成功的记录数
     */
    /**
     * 更新商品分类信息
     * 1. 更新商品分类基本信息
     * 2. 更新关联商品的分类名称
     * 3. 更新分类与属性的关联关系
     * 
     * @param id 要更新的商品分类ID
     * @param pmsProductCategoryParam 商品分类参数
     * @return 更新成功的记录数
     */
    @Override
    public int update(Long id, PmsProductCategoryParam pmsProductCategoryParam) {
        // 1. 创建商品分类对象并设置ID
        PmsProductCategory productCategory = new PmsProductCategory();
        productCategory.setId(id);
        // 2. 复制参数中的属性值到商品分类对象
        BeanUtils.copyProperties(pmsProductCategoryParam, productCategory);
        // 3. 更新分类层级
        setCategoryLevel(productCategory);
        
        // 4. 更新关联商品的分类名称
        PmsProduct product = new PmsProduct();
        product.setProductCategoryName(productCategory.getName());
        PmsProductExample example = new PmsProductExample();
        example.createCriteria().andProductCategoryIdEqualTo(id);
        productMapper.updateByExampleSelective(product, example);
        
        // 5. 处理商品分类与属性的关联关系
        // 5.1 先删除原有的关联关系
        PmsProductCategoryAttributeRelationExample relationExample = new PmsProductCategoryAttributeRelationExample();
        relationExample.createCriteria().andProductCategoryIdEqualTo(id);
        productCategoryAttributeRelationMapper.deleteByExample(relationExample);
        
        // 5.2 如果有新的属性ID列表，则创建新的关联关系
        if(!CollectionUtils.isEmpty(pmsProductCategoryParam.getProductAttributeIdList())){
            insertRelationList(id, pmsProductCategoryParam.getProductAttributeIdList());
        }
        
        // 6. 更新商品分类信息并返回结果
        return productCategoryMapper.updateByPrimaryKeySelective(productCategory);
    }

    /**
     * 分页查询商品分类
     * @param parentId 父分类ID
     * @param pageSize 每页大小
     * @param pageNum 页码
     * @return 商品分类列表
     */
    /**
     * 分页查询商品分类列表
     * 1. 设置分页参数
     * 2. 创建查询条件，按父分类ID查询
     * 3. 按sort字段降序排序
     * 4. 执行查询并返回结果
     * 
     * @param parentId 父分类ID，用于查询指定层级的分类
     * @param pageSize 每页记录数
     * @param pageNum 当前页码
     * @return 商品分类列表
     */
    @Override
    public List<PmsProductCategory> getList(Long parentId, Integer pageSize, Integer pageNum) {
        // 1. 设置分页参数
        PageHelper.startPage(pageNum, pageSize);
        
        // 2. 创建查询条件
        PmsProductCategoryExample example = new PmsProductCategoryExample();
        // 3. 设置排序规则：按sort字段降序
        example.setOrderByClause("sort desc");
        // 4. 设置查询条件：parentId等于指定值
        example.createCriteria().andParentIdEqualTo(parentId);
        
        // 5. 执行查询并返回结果
        return productCategoryMapper.selectByExample(example);
    }

    /**
     * 删除商品分类
     * @param id 商品分类ID
     * @return 删除成功的记录数
     */
    /**
     * 根据ID删除商品分类
     * 注意：此操作会级联删除该分类下的所有子分类
     * 
     * @param id 要删除的商品分类ID
     * @return 删除成功的记录数
     */
    @Override
    public int delete(Long id) {
        // 1. 根据主键ID删除商品分类记录
        // 注意：需要确保没有子分类或商品关联到此分类
        return productCategoryMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据ID获取商品分类详情
     * @param id 商品分类ID
     * @return 商品分类信息
     */
    /**
     * 根据ID获取商品分类详情
     * 
     * @param id 商品分类ID
     * @return 商品分类详细信息，如果不存在则返回null
     */
    @Override
    public PmsProductCategory getItem(Long id) {
        // 根据主键ID查询商品分类信息
        return productCategoryMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量更新导航栏显示状态
     * @param ids 商品分类ID列表
     * @param navStatus 导航栏显示状态：0->不显示；1->显示
     * @return 更新成功的记录数
     */
    /**
     * 批量更新商品分类的导航栏显示状态
     * 
     * @param ids 需要更新的商品分类ID列表
     * @param navStatus 导航栏显示状态：0->不显示；1->显示
     * @return 更新成功的记录数
     */
    @Override
    public int updateNavStatus(List<Long> ids, Integer navStatus) {
        // 1. 创建要更新的字段对象
        PmsProductCategory productCategory = new PmsProductCategory();
        // 2. 设置导航栏显示状态
        productCategory.setNavStatus(navStatus);
        
        // 3. 创建查询条件：ID在指定列表中
        PmsProductCategoryExample example = new PmsProductCategoryExample();
        example.createCriteria().andIdIn(ids);
        
        // 4. 执行批量更新并返回结果
        return productCategoryMapper.updateByExampleSelective(productCategory, example);
    }

    /**
     * 批量更新显示状态
     * @param ids 商品分类ID列表
     * @param showStatus 显示状态：0->不显示；1->显示
     * @return 更新成功的记录数
     */
    /**
     * 批量更新商品分类的显示状态
     * 
     * @param ids 需要更新的商品分类ID列表
     * @param showStatus 显示状态：0->不显示；1->显示
     * @return 更新成功的记录数
     */
    @Override
    public int updateShowStatus(List<Long> ids, Integer showStatus) {
        // 1. 创建要更新的字段对象
        PmsProductCategory productCategory = new PmsProductCategory();
        // 2. 设置显示状态
        productCategory.setShowStatus(showStatus);
        
        // 3. 创建查询条件：ID在指定列表中
        PmsProductCategoryExample example = new PmsProductCategoryExample();
        example.createCriteria().andIdIn(ids);
        
        // 4. 执行批量更新并返回结果
        return productCategoryMapper.updateByExampleSelective(productCategory, example);
    }

    /**
     * 查询所有一级分类及子分类（树形结构）
     * @return 包含子分类的商品分类列表
     */
    /**
     * 获取所有一级分类及其子分类（树形结构）
     * 使用递归方式构建分类的树形结构
     * 
     * @return 包含子分类的商品分类列表（树形结构）
     */
    @Override
    public List<PmsProductCategoryWithChildrenItem> listWithChildren() {
        // 调用DAO层方法获取分类树
        return productCategoryDao.listWithChildren();
    }

    /**
     * 根据分类的parentId设置分类的level
     * @param productCategory 商品分类实体
     */
    /**
     * 设置商品分类的层级
     * 1. 如果父分类ID为0，则设置为一级分类（level=0）
     * 2. 如果父分类ID不为0，则查询父分类的level，当前分类的level为父分类level+1
     * 3. 如果父分类不存在，则设置为一级分类
     * 
     * @param productCategory 商品分类对象
     */
    private void setCategoryLevel(PmsProductCategory productCategory) {
        // 1. 检查父分类ID
        if (productCategory.getParentId() == 0) {
            // 1.1 没有父分类，设置为一级分类
            productCategory.setLevel(0);
        } else {
            // 1.2 有父分类，查询父分类信息
            PmsProductCategory parentCategory = productCategoryMapper.selectByPrimaryKey(productCategory.getParentId());
            if (parentCategory != null) {
                // 1.2.1 父分类存在，设置当前分类的level为父分类level+1
                productCategory.setLevel(parentCategory.getLevel() + 1);
            } else {
                // 1.2.2 父分类不存在，设置为一级分类
                productCategory.setLevel(0);
            }
        }
    }
}
