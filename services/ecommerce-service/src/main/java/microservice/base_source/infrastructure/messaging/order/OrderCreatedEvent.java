package microservice.base_source.infrastructure.messaging.order;

public record OrderCreatedEvent (
        Long orderId,
        String buyerId,
        Long totalPrice
) {}
