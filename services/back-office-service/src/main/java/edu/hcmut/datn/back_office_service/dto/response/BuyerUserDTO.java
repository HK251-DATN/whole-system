package edu.hcmut.datn.back_office_service.dto.response;

import edu.hcmut.datn.back_office_service.common.enums.AccountStatus;
import edu.hcmut.datn.back_office_service.common.enums.Gender;
import edu.hcmut.datn.back_office_service.common.enums.MembershipLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyerUserDTO {
    
    // USER
    private String email;
    private String fName;
    private String lName;
    private String avtUrl;
    private LocalDate dob;
    private String pNum;
    private Gender gender;
    private AccountStatus accStatus;
    
    // BUYER
    private Long buyerId;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long loyaltyPoint;
    private Long totalOrders;
    private Long totalSpentAmount;
    private MembershipLevel membershipLevel;
}
