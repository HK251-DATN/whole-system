package edu.hcmut.datn.back_office_service.dto.request;

import edu.hcmut.datn.back_office_service.dao.EnterpriseStore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseStoreCreateRequest {

    private String storeName;
    private String storeDes;
    private Long providerId;

    public EnterpriseStore toEntity() {
        return new EnterpriseStore(storeName, storeDes, providerId);
    }
}
