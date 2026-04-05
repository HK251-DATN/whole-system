package microservice.base_source.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "BATCH_DETAIL")
public class BatchDetail {
	@Id
	@Column(name = "batch_detail_id", nullable = false)
	private String batchDetailId; // batch_id

	@Column(name = "product_general_id")
	private Long productGeneralId;

	@Column(name = "quantity")
	private int quantity;

	@Column(name = "price", precision = 19, scale = 4)
	private BigDecimal price;

	@Column(name = "avg_rate", precision = 19, scale = 4)
	private BigDecimal avgRate;

	@Column(name = "num_rate")
	private int numRate;

	@Column(name = "detail_content", columnDefinition = "TEXT")
    private String detailContent;

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
}
