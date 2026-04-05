package microservice.base_source.infrastructure.messaging.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservice.base_source.domain.entity.Category;
import microservice.base_source.domain.use_case.CategoryUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class CategoryCreatedConsumer {
    
    private final CategoryUseCase categoryUseCase;
    
    @KafkaListener(topics = "category-events")
    @Transactional
    public void consume(CategoryCreatedEvent event) {
        log.info("Received CategoryCreatedEvent: categoryId={}, name={}, isSubCategory={}",
                event.categoryId(), event.name(), event.isSubCategory());
        
        try {
            // Convert event to Category entity
            Category category = new Category();
            category.setCategoryId(event.categoryId());
            category.setCategoryName(event.name());
            category.setDescription(event.description());
            category.setDisplayOrder(event.displayOrder());
            category.setIconUrl(event.iconUrl());
            category.setIsSubCategory(event.isSubCategory());
            category.setBelongToCategory(event.belongToCategory());
            
            // Save category
            categoryUseCase.createFromEvent(category);
            
            log.info("Successfully created category {} in ecommerce service", event.categoryId());
            
        } catch (Exception e) {
            log.error("Failed to create category {} in ecommerce service: {}",
                    event.categoryId(), e.getMessage(), e);
            throw e; // Re-throw to trigger Kafka retry if configured
        }
    }
}