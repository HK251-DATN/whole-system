package microservice.base_source.infrastructure.messaging.buyer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservice.base_source.domain.entity.Cart;
import microservice.base_source.domain.use_case.BuyerUseCase;
import microservice.base_source.domain.use_case.CartUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@AllArgsConstructor
public class BuyerCreateConsumer {

    private final BuyerUseCase buyerUseCase;
    private final CartUseCase cartUseCase;

    @KafkaListener(topics = "user-events")
    @Transactional
    public void consume(BuyerCreateEvent event) {
        log.info("Received BuyerCreateEvent: {}", event);

        try {
            // 1. Create Buyer
            buyerUseCase.create(event.toBuyerEntity());
            log.info("Created BUYER {} successfully", event.getBuyerId());

            // 2. Auto-create Cart for the new Buyer
            Cart cart = new Cart();
            cart.setBuyerId(event.getBuyerId());

            Cart createdCart = cartUseCase.create(cart);
            log.info("Auto-created CART {} for BUYER {} successfully",
                    createdCart.getCartId(), event.getBuyerId());

        } catch (Exception e) {
            log.error("Failed to create BUYER {} or CART due to: {}",
                    event.getBuyerId(), e.getMessage(), e);
            throw e; // Re-throw to trigger Kafka retry if configured
        }
    }
}