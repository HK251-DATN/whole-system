package microservice.base_source.infrastructure.messaging.orderitem;

import java.math.BigDecimal;

public record OrderItemCreatedEvent(
        Long orderItemId,
        Long orderId,
        String batchDetailId,
        Long quantity,
        BigDecimal unitPriceAtPurchase,
        String buyerId
) {}