package edu.hcmut.datn.back_office_service.dto.request;

import edu.hcmut.datn.back_office_service.common.enums.DemandResponseStatus;
import edu.hcmut.datn.back_office_service.common.enums.Unit;
import edu.hcmut.datn.back_office_service.dao.DemandResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandResponseCreateRequest {
    private DemandResponseStatus status;
    private Long quantity;
    private Unit unit;
    private Long prodRqstId;
    private Long providerId;

    public DemandResponse toEntity() {
        return new DemandResponse(status, quantity, unit, prodRqstId, providerId);
    }
}
