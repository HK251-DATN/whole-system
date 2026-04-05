package microservice.base_source.infrastructure.messaging.productgeneral;

import microservice.base_source.domain.entity.ProductGeneral;

public record ProductGeneralCreatedEvent(
        Long prodGenId,
        String prodName,
        String imgUrl,
        String description,
        ProductGeneral.Unit unit,
        Long unitQuantity,
        Long subSubcategoryId,  // Main field - what product actually links to
        Long categoryId        // Derived subcategory ID - this is what ecommerce uses
) {
    public ProductGeneral toEntity() {
        ProductGeneral productGeneral = new ProductGeneral();
        productGeneral.setProductGeneralId(prodGenId);
        productGeneral.setName(prodName);
        productGeneral.setDescription(description);
        productGeneral.setImg(imgUrl);
        productGeneral.setUnit(unit);
        productGeneral.setUnitQuantity(unitQuantity);
        productGeneral.setCategoryId(categoryId);  // Uses the derived subcategory ID
        return productGeneral;
    }
}