package edu.hcmut.datn.back_office_service.dto.request.event;

import edu.hcmut.datn.back_office_service.dao.ProductRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ProductRequestUpdateRequest {
    private Long quantity;
    private Long requiredAfterDays;

    public ProductRequest toEntity() {
        return new ProductRequest(quantity, requiredAfterDays);
    }
}
