package edu.hcmut.datn.back_office_service.dto.request;

import java.util.List;

import edu.hcmut.datn.back_office_service.common.enums.DiscountType;
import edu.hcmut.datn.back_office_service.dao.CouponPolicy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponPolicyCreateRequest {
    private List<Long> applicableCateIds;
    private DiscountType discountType;
    private Long discountVal;
    private Long maxDiscountAmount;
    private Long minOrderValue;
    private Long maxUsesPerAcc;
    private Long createdBy;

    public CouponPolicy toEntity() {
        return new CouponPolicy(applicableCateIds, discountType, discountVal, maxDiscountAmount, minOrderValue,
                maxUsesPerAcc, createdBy);
    }
}
