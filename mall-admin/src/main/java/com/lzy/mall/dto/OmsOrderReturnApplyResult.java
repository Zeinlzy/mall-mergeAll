package com.lzy.mall.dto;

import com.lzy.mall.model.OmsCompanyAddress;
import com.lzy.mall.model.OmsOrderReturnApply;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 申请信息封装
 */
public class OmsOrderReturnApplyResult extends OmsOrderReturnApply {
    @Getter
    @Setter
    @Schema(description = "公司收货地址")
    private OmsCompanyAddress companyAddress;
}
