package microservice.base_source.infrastructure.messaging.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservice.base_source.infrastructure.messaging.category.CategoryCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        kafkaTemplate.send("order-events", event.orderId().toString(), event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event", ex);
            } else {
                log.info("Event sent with offset {}",
                        result.getRecordMetadata().offset()
                );
            }
        });
    }
}