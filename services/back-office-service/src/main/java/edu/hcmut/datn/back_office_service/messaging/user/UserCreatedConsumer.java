package edu.hcmut.datn.back_office_service.messaging.user;

import edu.hcmut.datn.back_office_service.dao.Buyer;
import edu.hcmut.datn.back_office_service.dao.User;
import edu.hcmut.datn.back_office_service.service.BuyerService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import edu.hcmut.datn.back_office_service.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@AllArgsConstructor
public class UserCreatedConsumer {

    private final UserService userService;
    
    private final BuyerService buyerService;

    @KafkaListener(topics = "user-events")
    public void consume(UserCreatedEvent event) {
        log.info("Received event: {}", event);

        try {
            User newUser = event.toUserEntity();
            
            userService.create(newUser);

            log.info("Create user {} success", event.getUserId());
            
            Buyer buyer = event.toBuyerEntity();
            
            buyerService.create(buyer);
            
            log.info("Create buyer {} success", buyer.getBuyerId());
        } catch (Exception e) {
            log.error("Create user {} fail due to: {}", event.getUserId(), e.getMessage());
        }
    }
}
