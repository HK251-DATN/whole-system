package edu.hcmut.datn.back_office_service.service;

import java.util.List;

import edu.hcmut.datn.back_office_service.dao.CouponPolicy;

public interface CouponPolicyService {
    CouponPolicy create(CouponPolicy couponPolicy);

    CouponPolicy read(Long couponPolicyId);

    List<CouponPolicy> readAll(Integer pageNum, Integer pageSize);

    CouponPolicy update(Long couponPolicyId, CouponPolicy updateCouponPolicy);

    void delete(Long couponPolicyId);
}
