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
@Table(name = "enterprise_stores")
@NoArgsConstructor
public class EnterpriseStore {

    @Column(name = "store_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long storeId;

    @Column(name = "store_name")
    @Getter
    @Setter
    private String storeName;

    @Column(name = "store_des")
    @Getter
    @Setter
    private String storeDes;

    @Column(name = "provider_id")
    @Getter
    private Long providerId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // For creating purpose
    public EnterpriseStore(String storeName, String storeDes, Long providerId) {
        this.storeName = storeName;
        this.storeDes = storeDes;
        this.providerId = providerId;
    }

    // For updating purpose
    public EnterpriseStore(String storeName, String storeDes) {
        this.storeName = storeName;
        this.storeDes = storeDes;
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
