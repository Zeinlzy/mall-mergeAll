package com.lzy.mall.mapper;

import com.lzy.mall.model.SmsCouponProductCategoryRelation;
import com.lzy.mall.model.SmsCouponProductCategoryRelationExample;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SmsCouponProductCategoryRelationMapper {
    long countByExample(SmsCouponProductCategoryRelationExample example);

    int deleteByExample(SmsCouponProductCategoryRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsCouponProductCategoryRelation row);

    int insertSelective(SmsCouponProductCategoryRelation row);

    List<SmsCouponProductCategoryRelation> selectByExample(SmsCouponProductCategoryRelationExample example);

    SmsCouponProductCategoryRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") SmsCouponProductCategoryRelation row, @Param("example") SmsCouponProductCategoryRelationExample example);

    int updateByExample(@Param("row") SmsCouponProductCategoryRelation row, @Param("example") SmsCouponProductCategoryRelationExample example);

    int updateByPrimaryKeySelective(SmsCouponProductCategoryRelation row);

    int updateByPrimaryKey(SmsCouponProductCategoryRelation row);
}