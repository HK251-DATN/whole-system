package edu.hcmut.datn.back_office_service.dao;

import java.time.LocalDateTime;

import edu.hcmut.datn.back_office_service.common.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@NoArgsConstructor
public class Order {

    @Column(name = "order_id")
    @Id
    @Getter
    @Setter
    private Long orderId;

    @Column(name = "status")
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "owned_by")
    @Getter
    @Setter
    private Long ownedBy;

    @Column(name = "confirmed_by")
    @Getter
    @Setter
    private Long confirmedBy;

    @Column(name = "packaged_by")
    @Getter
    @Setter
    private Long packagedBy;

    @Column(name = "shipped_by")
    @Getter
    @Setter
    private Long shippedBy;
    
    @Column(name = "total_price")
    @Getter
    @Setter
    private Long totalPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Contructor for creating purpose
    public Order(Long orderId, OrderStatus status, Long ownedBy, Long confirmedBy, Long packagedBy, Long shippedBy) {
        this.orderId = orderId;
        this.status = status;
        this.ownedBy = ownedBy;
        this.confirmedBy = confirmedBy;
        this.packagedBy = packagedBy;
        this.shippedBy = shippedBy;
    }

    // Contructor for updating purpose
    public Order(OrderStatus status, Long confirmedBy, Long packagedBy, Long shippedBy) {
        this.status = status;
        this.confirmedBy = confirmedBy;
        this.packagedBy = packagedBy;
        this.shippedBy = shippedBy;
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
