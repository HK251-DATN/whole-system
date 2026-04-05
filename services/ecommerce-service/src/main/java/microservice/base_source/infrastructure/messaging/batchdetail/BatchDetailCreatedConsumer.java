package microservice.base_source.infrastructure.messaging.batchdetail;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservice.base_source.domain.service.BatchDetailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class BatchDetailCreatedConsumer {

    private final BatchDetailService batchDetailService;

    @KafkaListener(topics = "batch-detail-events")
    public void consume(BatchDetailCreateEvent event) {
        log.info("Received event: {}", event);

        try {
            batchDetailService.create(event.toBatchDetailEntity());

            log.info("Create batch-detail {} success", event.getBatchDetailId());
        } catch (Exception e) {
            log.error("Create batch-detail {} fail due to: {}", event.getBatchDetailId(), e.getMessage());
        }
    }
}
