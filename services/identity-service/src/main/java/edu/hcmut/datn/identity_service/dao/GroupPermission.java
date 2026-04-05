package edu.hcmut.datn.identity_service.dao;

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
import lombok.Setter;

@Entity
@Table(name = "group_permission")
public class GroupPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grp_per_id")
    private long groupPerId;

    @Column(name = "group_id")
    @Getter
    @Setter
    private Long groupId;

    @Column(name = "per_id")
    @Getter
    @Setter
    private Long perId;

    @Column(name = "is_active")
    @Getter
    @Setter
    private boolean isActive;

    @Column(name = "valid_until")
    @Getter
    @Setter
    private LocalDateTime validUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
