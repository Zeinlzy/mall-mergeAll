package com.lzy.mall.dao;

import com.lzy.mall.model.PmsMemberPrice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会员价格管理自定义Dao
 */
public interface PmsMemberPriceDao {
    /**
     * 批量创建
     */
    int insertList(@Param("list") List<PmsMemberPrice> memberPriceList);
}
