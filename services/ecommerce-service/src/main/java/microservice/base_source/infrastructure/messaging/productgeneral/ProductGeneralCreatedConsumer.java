package microservice.base_source.infrastructure.messaging.productgeneral;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservice.base_source.domain.entity.ProductGeneral;
import microservice.base_source.domain.use_case.ProductGeneralUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductGeneralCreatedConsumer {
    
    private final ProductGeneralUseCase productGeneralUseCase;
    
    @KafkaListener(topics = "product-general-events")
    @Transactional
    public void consume(ProductGeneralCreatedEvent event) {
        log.info("Received ProductGeneralCreatedEvent: prodGenId={}, categoryId={}",
                event.prodGenId(), event.categoryId());
        
        try {
            // Convert event to entity - uses categoryId (subcategory) from event
            ProductGeneral productGeneral = event.toEntity();
            
            // Save product general
            productGeneralUseCase.createFromEvent(productGeneral);
            
            log.info("Successfully created product general {} in ecommerce service",
                    event.prodGenId());
            
        } catch (Exception e) {
            log.error("Failed to create product general {} in ecommerce service: {}",
                    event.prodGenId(), e.getMessage(), e);
            throw e;
        }
    }
}