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
@Table(name = "SALE_PRODUCT")
public class SaleProduct {
	@Id
	@Column(name = "sale_event_id")
    private Long saleEventId;

	// @Id
	@Column(name = "batch_id")
	private String batchId; // composited primary key (sale_event_id, batchId)

	@Column(name = "dis_val", precision = 10, scale = 2)
	private BigDecimal disVal; // discount percent

	@Column(name = "max_qty")
	private Long maxQty; // max sale quantity

	@Column(name = "cur_qty")
	private Long curQty;

	@Column(name = "max_buy")
	private Long maxBuy; // max buy quantity per buyer

	@Column(name = "created_at")
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
