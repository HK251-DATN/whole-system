package edu.hcmut.datn.back_office_service.dao;

import java.time.LocalDateTime;

import edu.hcmut.datn.back_office_service.common.enums.Unit;
import jakarta.persistence.*;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "product_generals")
@NoArgsConstructor
public class ProductGeneral {

    @Id
    @Column(name = "prod_gen_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long prodGenId;

    @Column(name = "prod_name")
    @Getter
    @Setter
    private String prodName;

    @Column(name = "img")
    @Getter
    @Setter
    private String imgUrl;
    
    // array of tags
    @Column(name = "tags", columnDefinition = "text[]")
    @Setter
    @Getter
    private String[] tags;

    @Column(name = "description")
    @Getter
    @Setter
    private String description;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "unit")
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private Unit unit;
    
    @Column(name = "unit_quantity")
    @Getter
    @Setter
    private Long unitQuantity;
    

    @Column(name = "preorder_policy_id")
    @Getter
    @Setter
    private Long preorderPolicyId;

    @Column(name = "enterprise_store_id")
    @Getter
    @Setter
    private Long enterpriseStoreId;

    @Column(name = "sub_subcategory_id")
    @Getter
    @Setter
    private Long subSubcategoryId;

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
