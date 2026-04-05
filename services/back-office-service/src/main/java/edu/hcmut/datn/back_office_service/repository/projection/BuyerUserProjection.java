package edu.hcmut.datn.back_office_service.repository.projection;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.hcmut.datn.back_office_service.common.enums.AccountStatus;
import edu.hcmut.datn.back_office_service.common.enums.Gender;
import edu.hcmut.datn.back_office_service.common.enums.MembershipLevel;
import edu.hcmut.datn.back_office_service.dao.Buyer;
import edu.hcmut.datn.back_office_service.dao.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface BuyerUserProjection {
    
    // USER
    String getEmail();
    @JsonProperty("fName")
    String getFName();
    
    @JsonProperty("lName")
    String getLName();
    String getAvtUrl();
    LocalDate getDob();
    
    @JsonProperty("pNum")
    String getPNum();
    Gender getGender();
    AccountStatus getAccStatus();
    
    // BUYER
    Long getBuyerId();
    Long getUserId();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    Long getLoyaltyPoint();
    Long getTotalOrders();
    Long getTotalSpentAmount();
    MembershipLevel getMembershipLevel();
}
