package edu.hcmut.datn.back_office_service.messaging.category;

public record SubSubcategoryCreatedEvent(
        Long subSubcategoryId,
        String name,
        String description,
        String iconUrl,
        Long subcategoryId
) {}