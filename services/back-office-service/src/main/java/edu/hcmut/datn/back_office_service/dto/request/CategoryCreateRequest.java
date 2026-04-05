package edu.hcmut.datn.back_office_service.dto.request;

import edu.hcmut.datn.back_office_service.dao.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {
    
    private String name;
    
    private String description;
    
    private Integer displayOrder;
    
    private String iconUrl;
    
    private String isSubCategory; // "Y" or "N"
    
    private Long belongToCategory; // Parent category ID (for subcategories)
    
    public Category toEntity() {
        Category category = new Category();
        category.setName(this.name);
        category.setDescription(this.description);
        category.setDisplayOrder(this.displayOrder);
        category.setIconUrl(this.iconUrl);
        category.setIsSubCategory(this.isSubCategory != null ? this.isSubCategory : "N");
        category.setBelongToCategory(this.belongToCategory);
        return category;
    }
}