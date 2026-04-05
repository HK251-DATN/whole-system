package edu.hcmut.datn.back_office_service.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.CouponPolicy;
import edu.hcmut.datn.back_office_service.exception.couponpolicy.CouponPolicyNotFoundException;
import edu.hcmut.datn.back_office_service.repository.CouponPolicyRepository;
import edu.hcmut.datn.back_office_service.service.CouponPolicyService;

@Service
public class CouponPolicyServiceImpl implements CouponPolicyService {

    private final CouponPolicyRepository couponPolicyRepository;

    public CouponPolicyServiceImpl(CouponPolicyRepository couponPolicyRepository) {
        this.couponPolicyRepository = couponPolicyRepository;
    }

    @Override
    public CouponPolicy create(CouponPolicy couponPolicy) {
        return couponPolicyRepository.save(couponPolicy);
    }

    @Override
    public CouponPolicy read(Long couponPolicyId) {
        return couponPolicyRepository.findById(couponPolicyId)
                .orElseThrow(() -> new CouponPolicyNotFoundException("Coupon Policy Not Found"));
    }

    @Override
    public List<CouponPolicy> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<CouponPolicy> policies = couponPolicyRepository.findAll(pageable);

        return policies.toList();
    }

    @Override
    public CouponPolicy update(Long couponPolicyId, CouponPolicy updateCouponPolicy) {
        CouponPolicy cur = read(couponPolicyId);

        if (updateCouponPolicy.getApplicableCateIds() != null) {
            cur.setApplicableCateIds(updateCouponPolicy.getApplicableCateIds());
        }

        if (updateCouponPolicy.getDiscountType() != null) {
            cur.setDiscountType(updateCouponPolicy.getDiscountType());
        }

        if (updateCouponPolicy.getDiscountVal() != null) {
            cur.setDiscountVal(updateCouponPolicy.getDiscountVal());
        }

        if (updateCouponPolicy.getMaxDiscountAmount() != null) {
            cur.setMaxDiscountAmount(updateCouponPolicy.getMaxDiscountAmount());
        }

        if (updateCouponPolicy.getMinOrderValue() != null) {
            cur.setMinOrderValue(updateCouponPolicy.getMinOrderValue());
        }

        if (updateCouponPolicy.getMaxUsesPerAcc() != null) {
            cur.setMaxUsesPerAcc(updateCouponPolicy.getMaxUsesPerAcc());
        }

        return couponPolicyRepository.save(cur);
    }

    @Override
    public void delete(Long couponPolicyId) {
        couponPolicyRepository.delete(read(couponPolicyId));
    }
}
