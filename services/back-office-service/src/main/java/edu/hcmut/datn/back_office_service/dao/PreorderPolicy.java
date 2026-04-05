package edu.hcmut.datn.back_office_service.dao;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "preorder_policies")
@NoArgsConstructor
public class PreorderPolicy {

    @Column(name = "preorder_policy_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long preorderPolicyId;

    @Column(name = "is_active")
    @Getter
    @Setter
    private Boolean isActive;

    @Column(name = "require_payment")
    @Getter
    @Setter
    private Boolean requirePayment;

    @Column(name = "deposit_percentage")
    @Getter
    @Setter
    private Long depositPercentage;

    @Column(name = "min_preorder_day")
    @Getter
    @Setter
    private Long minPreorderDay;

    @Column(name = "allow_cancel")
    @Getter
    @Setter
    private Boolean allowCancel;

    @Column(name = "notes")
    @Getter
    @Setter
    private String notes;

    @Column(name = "cancel_deadline")
    @Getter
    @Setter
    private Long cancelDeadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    @Getter
    private Long createdBy;

    // For creating purposes
    public PreorderPolicy(Boolean isActive, Boolean requirePayment, Long depositPercentage, Long minPreorderDay,
            Boolean allowCancel, String notes, Long cancelDeadline, Long createdBy) {
        this.isActive = isActive;
        this.requirePayment = requirePayment;
        this.depositPercentage = depositPercentage;
        this.minPreorderDay = minPreorderDay;
        this.allowCancel = allowCancel;
        this.notes = notes;
        this.cancelDeadline = cancelDeadline;
        this.createdBy = createdBy;
    }

    // For updating purposes
    public PreorderPolicy(Boolean isActive, Boolean requirePayment, Long depositPercentage, Long minPreorderDay,
            Boolean allowCancel, String notes, Long cancelDeadline) {
        this.isActive = isActive;
        this.requirePayment = requirePayment;
        this.depositPercentage = depositPercentage;
        this.minPreorderDay = minPreorderDay;
        this.allowCancel = allowCancel;
        this.notes = notes;
        this.cancelDeadline = cancelDeadline;
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
