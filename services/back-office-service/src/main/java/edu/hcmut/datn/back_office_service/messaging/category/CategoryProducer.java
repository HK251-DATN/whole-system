package edu.hcmut.datn.back_office_service.messaging.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishCategoryCreated(CategoryCreatedEvent event) {
        kafkaTemplate.send("category-events", event.categoryId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish CategoryCreatedEvent", ex);
                    } else {
                        log.info("CategoryCreatedEvent sent with offset {}",
                                result.getRecordMetadata().offset());
                    }
                });
    }
    
    public void publishSubSubcategoryCreated(SubSubcategoryCreatedEvent event) {
        kafkaTemplate.send("subsubcategory-events", event.subSubcategoryId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish SubSubcategoryCreatedEvent", ex);
                    } else {
                        log.info("SubSubcategoryCreatedEvent sent with offset {}",
                                result.getRecordMetadata().offset());
                    }
                });
    }
}