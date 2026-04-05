package edu.hcmut.datn.back_office_service.dto.request;

import edu.hcmut.datn.back_office_service.common.enums.DemandResponseStatus;
import edu.hcmut.datn.back_office_service.dao.DemandResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandResponseUpdateRequest {
    private DemandResponseStatus status;

    public DemandResponse toEntity() {
        return new DemandResponse(status);
    }

}
