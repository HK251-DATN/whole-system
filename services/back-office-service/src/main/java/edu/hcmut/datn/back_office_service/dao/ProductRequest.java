package edu.hcmut.datn.back_office_service.dao;

import java.time.LocalDateTime;

import edu.hcmut.datn.back_office_service.common.enums.Unit;
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
import lombok.ToString;

@Entity
@Table(name = "product_requests")
@NoArgsConstructor
@ToString
public class ProductRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prod_request_id")
    @Getter
    private Long prodRequestId;

    @Enumerated(EnumType.STRING)
    @Getter
    @Column(name = "unit")
    private Unit unit;

    @Getter
    @Setter
    @Column(name = "quantity")
    private Long quantity;

    @Getter
    @Setter
    @Column(name = "required_after_days")
    private Long requiredAfterDays;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Getter
    @Column(name = "prod_gen_id")
    private Long prodGenId;

    @Getter
    @Setter
    @Column(name = "event_id")
    private Long eventId;

    public ProductRequest(Unit unit, Long quantity, Long requiredAfterDays, Long prodGenId, Long eventId) {
        this.unit = unit;
        this.quantity = quantity;
        this.requiredAfterDays = requiredAfterDays;
        this.prodGenId = prodGenId;
        this.eventId = eventId;
    }

    public ProductRequest(Unit unit, Long quantity, Long requiredAfterDays, Long prodGenId) {
        this.unit = unit;
        this.quantity = quantity;
        this.requiredAfterDays = requiredAfterDays;
        this.prodGenId = prodGenId;
    }

    public ProductRequest(Long quantity, Long requiredAfterDays) {
        this.quantity = quantity;
        this.requiredAfterDays = requiredAfterDays;
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
