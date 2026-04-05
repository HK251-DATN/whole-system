package edu.hcmut.datn.back_office_service.controller;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.hcmut.datn.back_office_service.dao.CouponPolicy;
import edu.hcmut.datn.back_office_service.dto.request.CouponPolicyCreateRequest;
import edu.hcmut.datn.back_office_service.dto.request.CouponPolicyUpdateRequest;
import edu.hcmut.datn.back_office_service.dto.response.ApiResponse;
import edu.hcmut.datn.back_office_service.service.CouponPolicyService;

@Controller
@RequestMapping("/api/coupon-policy")
@RequiredArgsConstructor
public class CouponPolicyController {

    private final CouponPolicyService couponPolicyService;

    @PostMapping
    public ResponseEntity<ApiResponse<CouponPolicy>> create(@RequestBody CouponPolicyCreateRequest request) {
        try {
            CouponPolicy newPolicy = couponPolicyService.create(request.toEntity());

            return ResponseEntity.ok().body(
                    ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create coupon policy successfully", newPolicy));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{couponPolicyId}")
    public ResponseEntity<ApiResponse<CouponPolicy>> read(@PathVariable Long couponPolicyId) {
        try {
            CouponPolicy policy = couponPolicyService.read(couponPolicyId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read coupon policy successfully", policy));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponPolicy>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        List<CouponPolicy> policies = couponPolicyService.readAll(pageNum, pageSize);

        if (policies.isEmpty()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No Coupon Policy Exists", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all coupon policies successfully", policies));
    }

    @PutMapping("/{couponPolicyId}")
    public ResponseEntity<ApiResponse<CouponPolicy>> update(@PathVariable Long couponPolicyId,
            @RequestBody CouponPolicyUpdateRequest request) {
        try {
            CouponPolicy policy = couponPolicyService.update(couponPolicyId, request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update coupon policy successfully", policy));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{couponPolicyId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long couponPolicyId) {
        try {
            couponPolicyService.delete(couponPolicyId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete coupon policy successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

}
