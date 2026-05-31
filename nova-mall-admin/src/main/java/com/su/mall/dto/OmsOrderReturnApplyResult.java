package com.su.mall.dto;

import com.su.mall.model.OmsCompanyAddress;
import com.su.mall.model.OmsOrderReturnApply;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 申请信息封装
 * @author Su
 */
public class OmsOrderReturnApplyResult extends OmsOrderReturnApply {
    @Getter
    @Setter
    @Schema(title =  "公司收货地址")
    private OmsCompanyAddress companyAddress;
}
