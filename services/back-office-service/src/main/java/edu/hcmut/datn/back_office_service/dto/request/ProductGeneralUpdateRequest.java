package edu.hcmut.datn.back_office_service.dto.request;

import edu.hcmut.datn.back_office_service.common.enums.Unit;
import edu.hcmut.datn.back_office_service.dao.ProductGeneral;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductGeneralUpdateRequest {
    
    private String prodName;
    private String imgUrl;
    private String description;
    private Long preorderPolicyId;
    private Long enterpriseStoreId;
    private String[] tags;
    private Long subSubcategoryId;
    private Unit unit;
    private Long unitQuantity;
    
    public ProductGeneral toEntity() {
        ProductGeneral productGeneral = new ProductGeneral();
        
        productGeneral.setProdName(prodName);
        productGeneral.setImgUrl(imgUrl);
        productGeneral.setDescription(description);
        productGeneral.setPreorderPolicyId(preorderPolicyId);
        productGeneral.setEnterpriseStoreId(enterpriseStoreId);
        productGeneral.setSubSubcategoryId(subSubcategoryId);
        productGeneral.setTags(tags);
        productGeneral.setUnit(unit);
        productGeneral.setUnitQuantity(unitQuantity);
        
        return productGeneral;
    }
}