package edu.hcmut.datn.back_office_service.messaging.category;

public record CategoryCreatedEvent(
        Long categoryId,
        String name,
        String description,
        Integer displayOrder,
        String iconUrl,
        String isSubCategory,
        Long belongToCategory
) {}