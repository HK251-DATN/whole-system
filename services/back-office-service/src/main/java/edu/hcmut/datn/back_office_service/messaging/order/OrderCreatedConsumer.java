package edu.hcmut.datn.back_office_service.messaging.order;

import edu.hcmut.datn.back_office_service.dao.Buyer;
import edu.hcmut.datn.back_office_service.dao.Order;
import edu.hcmut.datn.back_office_service.dao.User;
import edu.hcmut.datn.back_office_service.messaging.user.UserCreatedEvent;
import edu.hcmut.datn.back_office_service.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class OrderCreatedConsumer {
    
    private final OrderService orderService;
    
    @KafkaListener(topics = "order-events")
    public void consume(OrderCreatedEvent event) {
        log.info("Received event: {}", event);
        
        try {
            Order newOrder = event.toOrderEntity();
            
            orderService.create(newOrder);
            
            log.info("Create order {} success", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Create order {} fail due to: {}", event.getOrderId(), e.getMessage());
        }
    }
}
