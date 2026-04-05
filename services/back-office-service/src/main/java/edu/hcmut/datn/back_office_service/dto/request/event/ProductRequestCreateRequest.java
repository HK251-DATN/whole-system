package edu.hcmut.datn.back_office_service.dto.request.event;

import edu.hcmut.datn.back_office_service.common.enums.Unit;
import edu.hcmut.datn.back_office_service.dao.ProductRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ProductRequestCreateRequest extends EventCreateRequest {

    private Unit unit;
    private Long quantity;
    private Long requiredAfterDays;
    private Long prodGenId;

    public ProductRequest toProductRequestEntity() {
        return new ProductRequest(unit, quantity, requiredAfterDays, prodGenId);
    }

    @Override
    public String toString() {
        return super.toString() + "ProductRequestCreateRequest [unit=" + unit + ", quantity=" + quantity + ", requiredAfterDays="
                + requiredAfterDays + ", prodGenId=" + prodGenId + "]";
    }

}
