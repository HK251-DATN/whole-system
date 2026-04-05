package microservice.base_source.infrastructure.messaging.orderitem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderItemProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishOrderItemCreated(OrderItemCreatedEvent event) {
        kafkaTemplate.send("order-item-events", event.orderItemId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish OrderItemCreatedEvent for orderItemId: {}",
                                event.orderItemId(), ex);
                    } else {
                        log.info("OrderItemCreatedEvent published successfully for orderItemId: {} with offset: {}",
                                event.orderItemId(),
                                result.getRecordMetadata().offset()
                        );
                    }
                });
    }
}