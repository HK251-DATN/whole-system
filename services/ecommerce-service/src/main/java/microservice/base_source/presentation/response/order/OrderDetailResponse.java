package microservice.base_source.presentation.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.Order;
import microservice.base_source.domain.entity.OrderItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponse {
    
    // Order information
    private Long orderId;
    private String buyerId;
    private Long addressId;
    private String status;
    private String note;
    private String type;
    private Long totalPrice;
    private String couponId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Order items
    private List<OrderItemResponse> orderItems;
    
    public static OrderDetailResponse fromEntity(Order order, List<OrderItem> orderItems) {
        return OrderDetailResponse.builder()
                .orderId(order.getOrderId())
                .buyerId(order.getBuyerId())
                .addressId(order.getAddressId())
                .status(order.getStatus() != null ? order.getStatus().toString() : null)
                .note(order.getNote())
                .type(order.getType() != null ? order.getType().toString() : null)
                .totalPrice(order.getTotalPrice())
                .couponId(order.getCouponId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(orderItems.stream()
                        .map(OrderItemResponse::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}