package edu.hcmut.datn.back_office_service.dto.request;

import edu.hcmut.datn.back_office_service.dao.PreorderPolicy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreorderPolicyUpdateRequest {
    private Boolean isActive;
    private Boolean requirePayment;
    private Long depositPercentage;
    private Long minPreorderDay;
    private Boolean allowCancel;
    private String notes;
    private Long cancelDeadline;

    public PreorderPolicy toEntity() {
        return new PreorderPolicy(isActive, requirePayment, depositPercentage, minPreorderDay, allowCancel, notes,
                cancelDeadline);
    }
}
