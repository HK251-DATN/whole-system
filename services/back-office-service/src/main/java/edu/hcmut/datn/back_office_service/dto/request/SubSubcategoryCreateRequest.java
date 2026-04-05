package edu.hcmut.datn.back_office_service.dto.request;

import edu.hcmut.datn.back_office_service.dao.SubSubcategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubSubcategoryCreateRequest {
    
    private String name;
    
    private String description;
    
    private String iconUrl;
    
    private Long subcategoryId;
    
    public SubSubcategory toEntity() {
        SubSubcategory subSubcategory = new SubSubcategory();
        subSubcategory.setName(this.name);
        subSubcategory.setDescription(this.description);
        subSubcategory.setIconUrl(this.iconUrl);
        subSubcategory.setSubcategoryId(this.subcategoryId);
        return subSubcategory;
    }
}