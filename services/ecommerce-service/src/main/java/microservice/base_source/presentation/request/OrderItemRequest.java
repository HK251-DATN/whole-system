package microservice.base_source.presentation.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.OrderItem;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
	private Long orderId;

	@NotBlank
	private String batchId;

	@NotNull
	private Long productGeneralId;
	
	private Long productDetailId;
	
	@NotBlank
	private Long quantity;
	
	@NotNull
	private BigDecimal originalPrice;
	
	private BigDecimal salePrice;

	private Long saleEventId;

	public OrderItem toEntity() {
		OrderItem item = new OrderItem();
//		item.setOrderId(this.orderId);
//		item.setBatchId(this.batchId);
//		item.setProductGeneralId(this.productGeneralId);
//		item.setProductDetailId(this.productDetailId);
//		item.setQuantity(this.quantity);
//		item.setOriginalPrice(this.originalPrice);
//		item.setSalePrice(this.salePrice);
		return item;
	}
}