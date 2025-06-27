package com.lzy.mall.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.lzy.mall.dao.*;
import com.lzy.mall.dto.PmsProductParam;
import com.lzy.mall.dto.PmsProductQueryParam;
import com.lzy.mall.dto.PmsProductResult;
import com.lzy.mall.mapper.*;
import com.lzy.mall.model.*;
import com.lzy.mall.service.PmsProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品管理Service实现类
 * 负责商品的增删改查、上下架、审核等核心业务逻辑
 */
@Service
public class PmsProductServiceImpl implements PmsProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PmsProductServiceImpl.class);
    
    // ========== 商品基础信息相关 ==========
    @Autowired
    private PmsProductMapper productMapper;
    @Autowired
    private PmsProductDao productDao;
    
    // ========== 会员价格相关 ==========
    @Autowired
    private PmsMemberPriceDao memberPriceDao;
    @Autowired
    private PmsMemberPriceMapper memberPriceMapper;
    
    // ========== 商品阶梯价格相关 ==========
    @Autowired
    private PmsProductLadderDao productLadderDao;
    @Autowired
    private PmsProductLadderMapper productLadderMapper;
    
    // ========== 商品满减相关 ==========
    @Autowired
    private PmsProductFullReductionDao productFullReductionDao;
    @Autowired
    private PmsProductFullReductionMapper productFullReductionMapper;
    
    // ========== SKU库存相关 ==========
    @Autowired
    private PmsSkuStockDao skuStockDao;
    @Autowired
    private PmsSkuStockMapper skuStockMapper;
    
    // ========== 商品属性值相关 ==========
    @Autowired
    private PmsProductAttributeValueDao productAttributeValueDao;
    @Autowired
    private PmsProductAttributeValueMapper productAttributeValueMapper;
    
    // ========== 商品专题相关 ==========
    @Autowired
    private CmsSubjectProductRelationDao subjectProductRelationDao;
    @Autowired
    private CmsSubjectProductRelationMapper subjectProductRelationMapper;
    
    // ========== 优选专区商品相关 ==========
    @Autowired
    private CmsPrefrenceAreaProductRelationDao prefrenceAreaProductRelationDao;
    @Autowired
    private CmsPrefrenceAreaProductRelationMapper prefrenceAreaProductRelationMapper;
    
    // ========== 商品审核相关 ==========
    @Autowired
    private PmsProductVertifyRecordDao productVertifyRecordDao;

    /**
     * 创建商品
     * @param productParam 商品参数
     * @return 创建成功返回1，失败返回0
     */
    @Override //测试成功
    public int create(PmsProductParam productParam) {
        int count;
        // 1. 创建商品基本信息
        PmsProduct product = productParam;
        product.setId(null);
        productMapper.insertSelective(product);
        
        // 2. 获取新创建的商品ID
        Long productId = product.getId();
        
        // 3. 处理商品价格相关设置
        // 3.1 设置会员价格
        relateAndInsertList(memberPriceDao, productParam.getMemberPriceList(), productId);
        // 3.2 设置阶梯价格
        relateAndInsertList(productLadderDao, productParam.getProductLadderList(), productId);
        // 3.3 设置满减价格
        relateAndInsertList(productFullReductionDao, productParam.getProductFullReductionList(), productId);
        
        // 4. 处理SKU信息
        // 4.1 生成SKU编码
        handleSkuStockCode(productParam.getSkuStockList(), productId);
        // 4.2 添加SKU库存信息
        relateAndInsertList(skuStockDao, productParam.getSkuStockList(), productId);
        
        // 5. 处理商品属性和规格
        relateAndInsertList(productAttributeValueDao, productParam.getProductAttributeValueList(), productId);
        
        // 6. 处理商品关联信息
        // 6.1 关联专题
        relateAndInsertList(subjectProductRelationDao, productParam.getSubjectProductRelationList(), productId);
        // 6.2 关联优选专区
        relateAndInsertList(prefrenceAreaProductRelationDao, productParam.getPrefrenceAreaProductRelationList(), productId);
        
        count = 1; // 操作成功
        return count;
    }

    /**
     * 处理SKU库存编码
     * 如果SKU编码为空，则生成新的编码规则：日期(8位) + 商品ID(4位) + 索引(3位)
     * 
     * @param skuStockList SKU库存列表
     * @param productId 商品ID
     */
    private void handleSkuStockCode(List<PmsSkuStock> skuStockList, Long productId) {
        if (CollectionUtils.isEmpty(skuStockList)) {
            return;
        }
        
        for (int i = 0; i < skuStockList.size(); i++) {
            PmsSkuStock skuStock = skuStockList.get(i);
            // 只有当SKU编码为空时才生成
            if (StrUtil.isEmpty(skuStock.getSkuCode())) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                StringBuilder sb = new StringBuilder();
                // 1. 添加日期部分(8位)
                sb.append(sdf.format(new Date()));
                // 2. 添加商品ID部分(4位数字，不足前面补0)
                sb.append(String.format("%04d", productId));
                // 3. 添加索引部分(3位数字，从001开始)
                sb.append(String.format("%03d", i + 1));
                // 4. 设置生成的SKU编码
                skuStock.setSkuCode(sb.toString());
            }
        }
    }

    /**
     * 获取商品编辑信息
     * @param id 商品ID
     * @return 包含商品完整信息的对象
     */
    @Override
    public PmsProductResult getUpdateInfo(Long id) {
        return productDao.getUpdateInfo(id);
    }

    /**
     * 更新商品信息
     * @param id 商品ID
     * @param productParam 商品参数
     * @return 更新成功返回1，失败返回0
     */
    @Override
    public int update(Long id, PmsProductParam productParam) {
        int count;
        
        // 1. 更新商品基本信息
        PmsProduct product = productParam;
        product.setId(id);
        productMapper.updateByPrimaryKeySelective(product);
        
        // 2. 更新会员价格
        updateMemberPrice(id, productParam);
        
        // 3. 更新阶梯价格
        updateProductLadder(id, productParam);
        
        // 4. 更新满减价格
        updateProductFullReduction(id, productParam);
        
        // 5. 更新SKU库存信息
        handleUpdateSkuStockList(id, productParam);
        
        // 6. 更新商品参数和规格
        updateProductAttributeValue(id, productParam);
        
        // 7. 更新商品关联的专题
        updateSubjectProductRelation(id, productParam);
        
        // 8. 更新商品关联的优选专区
        updatePrefrenceAreaProductRelation(id, productParam);
        
        count = 1; // 操作成功
        return count;
    }
    
    /**
     * 更新商品会员价格
     */
    private void updateMemberPrice(Long productId, PmsProductParam productParam) {
        // 删除原有的会员价格
        PmsMemberPriceExample pmsMemberPriceExample = new PmsMemberPriceExample();
        pmsMemberPriceExample.createCriteria().andProductIdEqualTo(productId);
        memberPriceMapper.deleteByExample(pmsMemberPriceExample);
        // 插入新的会员价格
        relateAndInsertList(memberPriceDao, productParam.getMemberPriceList(), productId);
    }
    
    /**
     * 更新商品阶梯价格
     */
    private void updateProductLadder(Long productId, PmsProductParam productParam) {
        // 删除原有的阶梯价格
        PmsProductLadderExample ladderExample = new PmsProductLadderExample();
        ladderExample.createCriteria().andProductIdEqualTo(productId);
        productLadderMapper.deleteByExample(ladderExample);
        // 插入新的阶梯价格
        relateAndInsertList(productLadderDao, productParam.getProductLadderList(), productId);
    }
    
    /**
     * 更新商品满减价格
     */
    private void updateProductFullReduction(Long productId, PmsProductParam productParam) {
        // 删除原有的满减价格
        PmsProductFullReductionExample fullReductionExample = new PmsProductFullReductionExample();
        fullReductionExample.createCriteria().andProductIdEqualTo(productId);
        productFullReductionMapper.deleteByExample(fullReductionExample);
        // 插入新的满减价格
        relateAndInsertList(productFullReductionDao, productParam.getProductFullReductionList(), productId);
    }
    
    /**
     * 更新商品参数和规格
     */
    private void updateProductAttributeValue(Long productId, PmsProductParam productParam) {
        // 删除原有的商品参数
        PmsProductAttributeValueExample productAttributeValueExample = new PmsProductAttributeValueExample();
        productAttributeValueExample.createCriteria().andProductIdEqualTo(productId);
        productAttributeValueMapper.deleteByExample(productAttributeValueExample);
        // 插入新的商品参数
        relateAndInsertList(productAttributeValueDao, productParam.getProductAttributeValueList(), productId);
    }
    
    /**
     * 更新商品关联的专题
     */
    private void updateSubjectProductRelation(Long productId, PmsProductParam productParam) {
        // 删除原有的专题关联
        CmsSubjectProductRelationExample subjectProductRelationExample = new CmsSubjectProductRelationExample();
        subjectProductRelationExample.createCriteria().andProductIdEqualTo(productId);
        subjectProductRelationMapper.deleteByExample(subjectProductRelationExample);
        // 插入新的专题关联
        relateAndInsertList(subjectProductRelationDao, productParam.getSubjectProductRelationList(), productId);
    }
    
    /**
     * 更新商品关联的优选专区
     */
    private void updatePrefrenceAreaProductRelation(Long productId, PmsProductParam productParam) {
        // 删除原有的优选专区关联
        CmsPrefrenceAreaProductRelationExample prefrenceAreaExample = new CmsPrefrenceAreaProductRelationExample();
        prefrenceAreaExample.createCriteria().andProductIdEqualTo(productId);
        prefrenceAreaProductRelationMapper.deleteByExample(prefrenceAreaExample);
        // 插入新的优选专区关联
        relateAndInsertList(prefrenceAreaProductRelationDao, productParam.getPrefrenceAreaProductRelationList(), productId);
    }

    /**
     * 处理更新SKU库存列表
     * 1. 处理新增的SKU
     * 2. 处理需要更新的SKU
     * 3. 处理需要删除的SKU
     * 
     * @param id 商品ID
     * @param productParam 商品参数
     */
    private void handleUpdateSkuStockList(Long id, PmsProductParam productParam) {
        // 1. 获取当前请求中的SKU列表
        List<PmsSkuStock> currSkuList = productParam.getSkuStockList();
        
        // 2. 如果当前SKU列表为空，则删除所有该商品的SKU
        if (CollUtil.isEmpty(currSkuList)) {
            PmsSkuStockExample skuStockExample = new PmsSkuStockExample();
            skuStockExample.createCriteria().andProductIdEqualTo(id);
            skuStockMapper.deleteByExample(skuStockExample);
            return;
        }
        
        // 3. 获取数据库中该商品原有的SKU列表
        PmsSkuStockExample skuStockExample = new PmsSkuStockExample();
        skuStockExample.createCriteria().andProductIdEqualTo(id);
        List<PmsSkuStock> oriSkuList = skuStockMapper.selectByExample(skuStockExample);
        
        // 4. 将SKU分为新增、更新、删除三类
        // 4.1 新增的SKU（ID为null）
        List<PmsSkuStock> insertSkuList = currSkuList.stream()
                .filter(item -> item.getId() == null)
                .collect(Collectors.toList());
                
        // 4.2 需要更新的SKU（ID不为null）
        List<PmsSkuStock> updateSkuList = currSkuList.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toList());
                
        // 4.3 获取需要更新的SKU ID集合
        List<Long> updateSkuIds = updateSkuList.stream()
                .map(PmsSkuStock::getId)
                .collect(Collectors.toList());
                
        // 4.4 需要删除的SKU（在数据库中存在但在当前请求中不存在的SKU）
        List<PmsSkuStock> removeSkuList = oriSkuList.stream()
                .filter(item -> !updateSkuIds.contains(item.getId()))
                .collect(Collectors.toList());
        
        // 5. 处理SKU编码
        handleSkuStockCode(insertSkuList, id);
        handleSkuStockCode(updateSkuList, id);
        
        // 6. 执行新增操作
        if (CollUtil.isNotEmpty(insertSkuList)) {
            relateAndInsertList(skuStockDao, insertSkuList, id);
        }
        
        // 7. 执行删除操作
        if (CollUtil.isNotEmpty(removeSkuList)) {
            List<Long> removeSkuIds = removeSkuList.stream()
                    .map(PmsSkuStock::getId)
                    .collect(Collectors.toList());
                    
            PmsSkuStockExample removeExample = new PmsSkuStockExample();
            removeExample.createCriteria().andIdIn(removeSkuIds);
            skuStockMapper.deleteByExample(removeExample);
        }
        
        // 8. 执行更新操作
        if (CollUtil.isNotEmpty(updateSkuList)) {
            for (PmsSkuStock pmsSkuStock : updateSkuList) {
                skuStockMapper.updateByPrimaryKeySelective(pmsSkuStock);
            }
        }

    }

    @Override
    public List<PmsProduct> list(PmsProductQueryParam productQueryParam, Integer pageSize, Integer pageNum) {
        // 启动分页插件，设置当前页码和每页大小
        // PageHelper 会拦截接下来的第一个 MyBatis 查询，并自动添加 LIMIT 子句进行分页
        PageHelper.startPage(pageNum, pageSize);

        // 创建PmsProductExample对象，用于构建动态查询条件
        PmsProductExample productExample = new PmsProductExample();
        // 创建查询条件构建器Criteria
        PmsProductExample.Criteria criteria = productExample.createCriteria();

        // 默认查询条件：商品删除状态为0 (未删除)
        criteria.andDeleteStatusEqualTo(0);

        // 根据查询参数动态添加条件
        // 如果查询参数中指定了发布状态，则添加发布状态的查询条件
        if (productQueryParam.getPublishStatus() != null) {
            criteria.andPublishStatusEqualTo(productQueryParam.getPublishStatus());
        }
        // 如果查询参数中指定了审核状态，则添加审核状态的查询条件
        if (productQueryParam.getVerifyStatus() != null) {
            criteria.andVerifyStatusEqualTo(productQueryParam.getVerifyStatus());
        }
        // 如果查询参数中指定了关键词（商品名称），且不为空，则添加模糊查询条件
        // StrUtil.isEmpty() 用于判断字符串是否为空或只包含空白字符
        if (!StrUtil.isEmpty(productQueryParam.getKeyword())) {
            // 使用 LIKE 进行模糊匹配，前后添加 % 表示任意匹配
            criteria.andNameLike("%" + productQueryParam.getKeyword() + "%");
        }
        // 如果查询参数中指定了货号，且不为空，则添加精确查询条件
        if (!StrUtil.isEmpty(productQueryParam.getProductSn())) {
            criteria.andProductSnEqualTo(productQueryParam.getProductSn());
        }
        // 如果查询参数中指定了品牌ID，则添加品牌ID的查询条件
        if (productQueryParam.getBrandId() != null) {
            criteria.andBrandIdEqualTo(productQueryParam.getBrandId());
        }
        // 如果查询参数中指定了商品分类ID，则添加商品分类ID的查询条件
        if (productQueryParam.getProductCategoryId() != null) {
            criteria.andProductCategoryIdEqualTo(productQueryParam.getProductCategoryId());
        }

        // 执行MyBatis查询：根据构建的Example条件查询PmsProduct列表
        // 由于之前调用了PageHelper.startPage()，这个查询会自动进行分页
        return productMapper.selectByExample(productExample);
    }

    @Override
    public int updateVerifyStatus(List<Long> ids, Integer verifyStatus, String detail) {
        // 1. 创建 PmsProduct 对象，用于设置要更新的字段
        PmsProduct product = new PmsProduct();
        // 设置商品的审核状态。这个状态将应用于所有指定id的商品。
        product.setVerifyStatus(verifyStatus);

        // 2. 创建 PmsProductExample 对象，用于构建更新条件
        PmsProductExample example = new PmsProductExample();
        // 创建条件：选择所有ID在传入的ids列表中的商品
        example.createCriteria().andIdIn(ids);

        // 3. 执行批量更新操作
        // 使用 productMapper 的 updateByExampleSelective 方法，根据 example 条件选择性地更新 product 对象中非空的字段
        // 这里的 'product' 只包含了 'verifyStatus' 字段，所以只会更新该字段。
        int count = productMapper.updateByExampleSelective(product, example);

        // 4. 准备审核记录列表
        List<PmsProductVertifyRecord> list = new ArrayList<>();
        // 遍历所有被更新的商品ID，为每个商品创建一个审核记录
        for (Long id : ids) {
            PmsProductVertifyRecord record = new PmsProductVertifyRecord();
            record.setProductId(id); // 记录商品ID
            record.setCreateTime(new Date()); // 记录当前审核时间
            record.setDetail(detail); // 记录审核详情（例如审核意见）
            record.setStatus(verifyStatus); // 记录审核结果状态
            record.setVertifyMan("test"); // 记录审核人，这里硬编码为"test"，实际应用中应获取当前登录用户
            list.add(record); // 将创建的审核记录添加到列表中
        }

        // 5. 批量插入审核记录
        // 调用 productVertifyRecordDao 的 insertList 方法，将所有审核记录批量插入到数据库中
        productVertifyRecordDao.insertList(list);

        // 6. 返回受影响的行数 (即更新的商品数量)
        return count;
    }

    @Override
    public int updatePublishStatus(List<Long> ids, Integer publishStatus) {
        // 1. 创建 PmsProduct 对象，用于指定要更新的字段及其新值。
        // 在这个场景中，我们只需要更新商品的 'publishStatus' 字段。
        PmsProduct record = new PmsProduct();
        record.setPublishStatus(publishStatus); // 设置商品的发布状态：0->下架；1->上架

        // 2. 创建 PmsProductExample 对象，用于构建数据库查询或更新的条件。
        PmsProductExample example = new PmsProductExample();
        // 使用 createCriteria() 方法获取一个 Criteria 对象，用于添加具体的查询条件。
        // andIdIn(ids) 表示 WHERE id IN (ids列表中的所有ID)。
        // 这样可以确保只有在传入的 'ids' 列表中的商品才会被更新。
        example.createCriteria().andIdIn(ids);

        // 3. 执行数据库更新操作。
        // productMapper.updateByExampleSelective(record, example) 方法会根据 'example' 中定义的条件，
        // 选择性地更新匹配到的记录。'Selective' 意味着只会更新 'record' 对象中非空的字段。
        // 在本例中，只有 'publishStatus' 字段会被更新。
        // 方法返回受影响的行数，即成功更新的商品数量。
        return productMapper.updateByExampleSelective(record, example);
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        PmsProduct record = new PmsProduct();
        record.setRecommandStatus(recommendStatus);
        PmsProductExample example = new PmsProductExample();
        example.createCriteria().andIdIn(ids);
        return productMapper.updateByExampleSelective(record, example);
    }

    @Override
    public int updateNewStatus(List<Long> ids, Integer newStatus) {
        PmsProduct record = new PmsProduct();
        record.setNewStatus(newStatus);
        PmsProductExample example = new PmsProductExample();
        example.createCriteria().andIdIn(ids);
        return productMapper.updateByExampleSelective(record, example);
    }

    @Override
    public int updateDeleteStatus(List<Long> ids, Integer deleteStatus) {
        PmsProduct record = new PmsProduct();
        record.setDeleteStatus(deleteStatus);
        PmsProductExample example = new PmsProductExample();
        example.createCriteria().andIdIn(ids);
        return productMapper.updateByExampleSelective(record, example);
    }

    @Override
    public List<PmsProduct> list(String keyword) {
        PmsProductExample productExample = new PmsProductExample();
        PmsProductExample.Criteria criteria = productExample.createCriteria();
        criteria.andDeleteStatusEqualTo(0);
        if(!StrUtil.isEmpty(keyword)){
            criteria.andNameLike("%" + keyword + "%");
            productExample.or().andDeleteStatusEqualTo(0).andProductSnLike("%" + keyword + "%");
        }
        return productMapper.selectByExample(productExample);
    }

    /**
     * 建立和插入关系表操作
     * 用于处理商品与相关实体（如属性、规格等）的关联关系，并将数据批量插入数据库
     *
     * @param dao       数据访问对象，用于执行数据库操作
     * @param dataList  需要插入的关联数据列表
     * @param productId 当前商品的ID，用于建立关联关系
     * @throws RuntimeException 当反射调用或数据库操作失败时抛出运行时异常
     */
    private void relateAndInsertList(Object dao, List dataList, Long productId) {
        try {
            // 如果数据列表为空，则直接返回
            if (CollectionUtils.isEmpty(dataList)) return;
            
            // 遍历数据列表，为每个关联对象设置ID和商品ID
            for (Object item : dataList) {
                // 使用反射调用setId方法，将ID设为null（由数据库自增生成）
                Method setId = item.getClass().getMethod("setId", Long.class);
                setId.invoke(item, (Long) null);
                /**
                 * Method setId = item.getClass().getMethod("setId", Long.class);
                 * 功能：获取方法对象
                 * item.getClass() - 获取item对象的Class对象（运行时类型）
                 * getMethod("setId", Long.class) - 从Class对象中查找名为"setId"的公共方法
                 * 第一个参数"setId"是方法名
                 * 第二个参数Long.class指定该方法接受一个Long类型的参数
                 * 返回一个Method对象，代表找到的setId方法
                 */
                /**
                 * setId.invoke(item, (Long) null);
                 * 功能：动态调用方法
                 * invoke()是Method类的方法，用于动态执行该方法
                 * 第一个参数item是要调用方法的目标对象
                 * 第二个参数(Long) null是传递给setId方法的参数值
                 * 这里传入了一个null值，并强制转换为Long类型
                 * 相当于调用item.setId(null)
                 */
                
                // 使用反射调用setProductId方法，设置商品ID建立关联关系
                Method setProductId = item.getClass().getMethod("setProductId", Long.class);
                setProductId.invoke(item, productId);
            }
            
            // 使用反射调用DAO的insertList方法，批量插入数据
            Method insertList = dao.getClass().getMethod("insertList", List.class);
            insertList.invoke(dao, dataList);
        } catch (Exception e) {
            // 记录错误日志并抛出运行时异常
            LOGGER.warn("创建商品出错:{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

}
