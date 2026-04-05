package microservice.base_source.presentation.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.Address;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    
    private String receiverName;
    
    private String receiverPNum;
    
    private String province;
    
    private String district;
    
    private String commune;
    
    private String detail;
    
    private Boolean isDefault;
    
    public Address toEntity() {
        Address address = new Address();
        
        address.setReceiverName(receiverName);
        address.setReceiverPNum(receiverPNum);
        address.setProvince(province);
        address.setDistrict(district);
        address.setCommune(commune);
        address.setDetail(detail);
        address.setIsDefault(isDefault);
        
        return address;
    }
}
