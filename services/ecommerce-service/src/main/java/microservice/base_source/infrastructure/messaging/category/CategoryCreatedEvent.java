package microservice.base_source.infrastructure.messaging.category;

public record CategoryCreatedEvent(
        Long categoryId,
        String name,
        String description,
        Integer displayOrder,
        String iconUrl,
        String isSubCategory,
        Long belongToCategory
) {}