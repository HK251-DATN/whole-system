package microservice.base_source.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PRODUCT_GENERAL")
public class ProductGeneral {
	@Id
    @Column(name = "product_general_id")
    private Long productGeneralId;

    // foreign key to Category
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "provider_id")
    private String providerId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // detail description

    @Column(name = "status")
    private String status;
    
    @Column(name = "unit")
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private Unit unit;
    
    @Column(name = "unit_quantity")
    @Getter
    @Setter
    private Long unitQuantity;

    // array of tags
    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;

    @Column(name = "img", columnDefinition = "TEXT")
    private String img; 

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "delete_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @PreRemove
    public void preRemove() {
        deletedAt = LocalDateTime.now();
    }
    
    public enum Unit {
        KILOGRAM,
        GRAM,
        PIECE,
        DOZEN,
        LITER,
        MILLILITER,
        PACK,
        BOX,
        BOTTLE;
    }
}

