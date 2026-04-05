package microservice.base_source.presentation.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.Order;
import microservice.base_source.domain.entity.OrderItem;
import microservice.base_source.domain.entity.Order.OrderStatus;
import microservice.base_source.domain.entity.Order.OrderType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
	@NotBlank
	private String buyerId;

	@NotNull
	private String note;

	@NotNull
	private OrderType type; // DEFAULT, PREORDER, CART //TODO create cart entity not put in order

	@NotEmpty
	List<OrderItemRequest> orderItems;

	private String couponId;

	public Order toOrderEntity() {
		Order order = new Order();
		order.setBuyerId(this.buyerId);
		order.setStatus(OrderStatus.PENDING);
		order.setNote(this.note);
		order.setType(this.type);
		order.setCouponId(couponId);
		return order;
	}

	public List<OrderItem> toListOrderItemEntity() {
		// Convert OrderItemRequest to OrderItem entities
		List<OrderItem> items = this.orderItems.stream()
			.map(OrderItemRequest::toEntity)
			.toList();
		return items;
	}
}
