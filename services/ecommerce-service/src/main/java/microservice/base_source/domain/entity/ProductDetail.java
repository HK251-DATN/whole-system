package microservice.base_source.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PRODUCT_DETAIL")
public class ProductDetail {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_detail_id")
    private Long productDetailId;

    // foreign key to ProductGeneral
	@Column(name = "product_general_id", nullable = false)
    private Long productGeneralId;

    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detail")
    private Object detail; // HTML, CSS
    
    @Column(name = "img", columnDefinition = "TEXT")
    private String img; // content detail info: HTML, CSS

    @Column(name = "status")
    private String status; // ACTIVE, DELETED

    @Column(name = "rating", precision = 10, scale = 2) // index
    private BigDecimal rating;

    @Column(name = "buy_yn")
    private String buyYn; // Y or N

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // index

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
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
}
