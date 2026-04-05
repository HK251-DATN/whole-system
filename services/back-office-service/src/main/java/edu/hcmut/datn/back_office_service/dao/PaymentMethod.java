package edu.hcmut.datn.back_office_service.dao;

import java.time.LocalDateTime;

import edu.hcmut.datn.back_office_service.common.enums.PaymentProvider;
import edu.hcmut.datn.back_office_service.common.enums.PaymentType;
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
@Table(name = "payment_methods")
@NoArgsConstructor
public class PaymentMethod {

    @Column(name = "payment_method_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long paymentMethodId;

    @Column(name = "payment_type")
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "payment_provider")
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private PaymentProvider paymentProvider;

    @Column(name = "account_num")
    @Getter
    @Setter
    private String accountNum;

    @Column(name = "is_active")
    @Getter
    @Setter
    private Boolean isActive;

    @Column(name = "is_default")
    @Getter
    @Setter
    private Boolean isDefault;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "buyer_id")
    @Getter
    @Setter
    private Long buyerId;

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
