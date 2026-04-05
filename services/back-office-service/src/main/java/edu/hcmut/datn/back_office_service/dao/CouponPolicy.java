package edu.hcmut.datn.back_office_service.dao;

import java.time.LocalDateTime;
import java.util.List;

import edu.hcmut.datn.back_office_service.common.enums.DiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coupon_policies")
@NoArgsConstructor
public class CouponPolicy {

    @Column(name = "coupon_policy_id")
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponPolicyId;

    @Column(name = "applicable_cate_ids")
    @Setter
    @Getter
    private List<Long> applicableCateIds;

    @Column(name = "discount_type")
    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(name = "discount_val")
    @Setter
    @Getter
    private Long discountVal;

    @Column(name = "max_discount_amount")
    @Setter
    @Getter
    private Long maxDiscountAmount;

    @Column(name = "min_order_value")
    @Setter
    @Getter
    private Long minOrderValue;

    @Column(name = "max_uses_per_acc")
    @Setter
    @Getter
    private Long maxUsesPerAcc;

    @Column(name = "cur_total_uses")
    @Getter
    private Long curTotalUses;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    @Getter
    private Long createdBy;

    public CouponPolicy(List<Long> applicableCateIds, DiscountType discountType, Long discountVal,
            Long maxDiscountAmount, Long minOrderValue, Long maxUsesPerAcc, Long createdBy) {
        this.applicableCateIds = applicableCateIds;
        this.discountType = discountType;
        this.discountVal = discountVal;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minOrderValue = minOrderValue;
        this.maxUsesPerAcc = maxUsesPerAcc;
        this.curTotalUses = 0L;
        this.createdBy = createdBy;
    }

    public CouponPolicy(List<Long> applicableCateIds, DiscountType discountType, Long discountVal,
            Long maxDiscountAmount, Long minOrderValue, Long maxUsesPerAcc) {
        this.applicableCateIds = applicableCateIds;
        this.discountType = discountType;
        this.discountVal = discountVal;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minOrderValue = minOrderValue;
        this.maxUsesPerAcc = maxUsesPerAcc;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(); // Set createdAt on first save
        updatedAt = LocalDateTime.now(); // Optional: Set initial updatedAt
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(); // Update on every save after creation
    }
}
