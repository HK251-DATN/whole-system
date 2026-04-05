package edu.hcmut.datn.identity_service.messaging.user;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserCreated(UserCreatedEvent event) {
        kafkaTemplate.send("user-events", event.userId().toString(), event).whenComplete((result, ex) -> {
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
