package microservice.base_source.presentation.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    
    private Long orderItemId;
    private Long orderId;
    private String batchDetailId;
    private Long quantity;
    private BigDecimal originalPrice;
    private BigDecimal unitPriceAtPurchase;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Optional: Product information (if needed)
    private String productName;
    
    public static OrderItemResponse fromEntity(OrderItem orderItem) {
        BigDecimal totalPrice = orderItem.getUnitPriceAtPurchase() != null
                ? orderItem.getUnitPriceAtPurchase().multiply(BigDecimal.valueOf(orderItem.getQuantity()))
                : BigDecimal.ZERO;
        
        return OrderItemResponse.builder()
                .orderItemId(orderItem.getOrderItemId())
                .orderId(orderItem.getOrderId())
                .batchDetailId(orderItem.getBatchDetailId())
                .quantity(orderItem.getQuantity())
                .originalPrice(orderItem.getOriginalPrice())
                .unitPriceAtPurchase(orderItem.getUnitPriceAtPurchase())
                .totalPrice(totalPrice)
                .createdAt(orderItem.getCreatedAt())
                .updatedAt(orderItem.getUpdatedAt())
                .build();
    }
    
    public static OrderItemResponse fromEntityWithProductName(
            OrderItem orderItem,
            String productName) {
        OrderItemResponse response = fromEntity(orderItem);
        response.setProductName(productName);
        return response;
    }
}