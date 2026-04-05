package microservice.base_source.infrastructure.messaging.buyer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import microservice.base_source.domain.entity.BatchDetail;
import microservice.base_source.domain.entity.Buyer;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuyerCreateEvent {
    
    @JsonProperty("userId")
    private String buyerId;
    
    @JsonProperty("fName")
    private String fName;
    
    @JsonProperty("lName")
    private String lName;
    
    @JsonProperty("email")
    private String email;
    
    public Buyer toBuyerEntity() {
        
        Buyer buyer = new Buyer();
        
        buyer.setBuyerId(buyerId);
        buyer.setFName(fName);
        buyer.setLName(lName);
        buyer.setEmail(email);
        
        return buyer;
    }
}