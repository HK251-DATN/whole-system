package edu.hcmut.datn.back_office_service.messaging.productgeneral;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductGeneralProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishProductGeneralCreated(ProductGeneralCreatedEvent event) {
        kafkaTemplate.send("product-general-events", event.prodGenId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish ProductGeneralCreatedEvent", ex);
                    } else {
                        log.info("ProductGeneralCreatedEvent sent with offset {}",
                                result.getRecordMetadata().offset());
                    }
                });
    }
}