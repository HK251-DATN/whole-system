package edu.hcmut.datn.back_office_service.dto.request;

import edu.hcmut.datn.back_office_service.common.enums.Bank;
import edu.hcmut.datn.back_office_service.common.enums.VerificationStatus;
import edu.hcmut.datn.back_office_service.dao.Provider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderUpdateRequest {
    private Long reputationPoint;

    private VerificationStatus verificationStatus;

    private Bank bankId;

    private String bankNum;

    public Provider toEntity() {
        return new Provider(
                reputationPoint,
                verificationStatus,
                bankId,
                bankNum);
    }
}
