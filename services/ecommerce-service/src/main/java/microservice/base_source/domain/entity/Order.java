package microservice.base_source.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
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
@Table(name = "ORDERS")
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_id", nullable = false)
    private Long orderId;

	@Column(name = "buyer_id")
	private String buyerId;

	@Column(name = "address_id")
	private Long addressId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private OrderStatus status; // PENDING, PAID, CONFIRMED, PACKAGED, SHIPPED, COMPLETED, CANCELLED

	@Column(name = "note")
	private String note;

	@Column(name = "type")
	private OrderType type; // DEFAULT, PREORDER, CART

	@Column(name = "total_price")
	private Long totalPrice;

	@Column(name = "coupon_id")
	private String couponId;

	@Column(name = "created_at")
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

	public enum OrderStatus {
		PENDING,
		PAID,
		CONFIRMED,
		PACKAGED,
		SHIPPED,
		COMPLETED,
		CANCELLED
	}

	public enum OrderType {
		DEFAULT,
		PREORDER,
		CART
	}
}
