package microservice.base_source.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
	private Long addressId;
    
    @Column(name = "receiver_name")
    private String receiverName;
    
    @Column(name = "receiver_p_num")
    private String receiverPNum;
    
    @Column(name = "province")
    private String province;
    
    @Column(name = "district")
    private String district;
    
    @Column(name = "commune")
    private String commune;
    
    @Column(name = "detail")
    private String detail;
    
    @Column(name = "is_default")
    private Boolean isDefault;
    
    @Column(name = "buyer_id")
    private String buyerId;
}
