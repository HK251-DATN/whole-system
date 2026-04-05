package edu.hcmut.datn.back_office_service.dao;

import java.time.LocalDateTime;

import edu.hcmut.datn.back_office_service.common.enums.DemandResponseStatus;
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

@Entity
@Table(name = "demand_responses")
@NoArgsConstructor
public class DemandResponse {

    @Column(name = "demand_resp_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long demandRespId;

    @Column(name = "status")
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private DemandResponseStatus status;

    @Column(name = "quantity")
    @Getter
    private Long quantity;

    @Column(name = "unit")
    @Getter
    @Enumerated(EnumType.STRING)
    private Unit unit;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "prodRqstId")
    @Getter
    private Long prodRqstId;

    @Column(name = "providerId")
    @Getter
    private Long providerId;

    public DemandResponse(
            DemandResponseStatus status,
            Long quantity,
            Unit unit,
            Long prodRqstId,
            Long providerId) {
        this.status = status;
        this.quantity = quantity;
        this.unit = unit;
        this.prodRqstId = prodRqstId;
        this.providerId = providerId;
    }

    public DemandResponse(
            DemandResponseStatus status) {
        this.status = status;
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
