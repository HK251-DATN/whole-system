package microservice.base_source.presentation.response.cartitem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.CartItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    
    private Long cartItemId;
    private Long cartId;
    private String batchDetailId;
    private Long quantity;
    private Boolean isSelected;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // BatchDetail information
    private String productName;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    
    public static CartItemResponse fromEntity(CartItem cartItem) {
        return CartItemResponse.builder()
                .cartItemId(cartItem.getCartItemId())
                .cartId(cartItem.getCartId())
                .batchDetailId(cartItem.getBatchDetailId())
                .quantity(cartItem.getQuantity())
                .isSelected(cartItem.getIsSelected())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
    
    public static CartItemResponse fromEntityWithBatchDetail(
            CartItem cartItem,
            String productName,
            BigDecimal unitPrice) {
        
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        
        return CartItemResponse.builder()
                .cartItemId(cartItem.getCartItemId())
                .cartId(cartItem.getCartId())
                .batchDetailId(cartItem.getBatchDetailId())
                .quantity(cartItem.getQuantity())
                .isSelected(cartItem.getIsSelected())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .productName(productName)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .build();
    }
}