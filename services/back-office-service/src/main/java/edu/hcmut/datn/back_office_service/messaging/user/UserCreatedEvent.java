package edu.hcmut.datn.back_office_service.messaging.user;

import java.time.LocalDate;

import edu.hcmut.datn.back_office_service.common.enums.AccountStatus;
import edu.hcmut.datn.back_office_service.common.enums.Gender;
import edu.hcmut.datn.back_office_service.dao.Buyer;
import edu.hcmut.datn.back_office_service.dao.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class UserCreatedEvent {

    @Getter
    private final Long userId;

    @Getter
    private final String email;

    @Getter
    private final String fName;

    @Getter
    private final String lName;

    @Getter
    private final String avtUrl;

    @Getter
    private final LocalDate dob;

    @Getter
    private final String pNum;

    @Getter
    private final Gender gender;

    public User toUserEntity() {
        User newUser = new User();

        newUser.setUserId(userId);
        newUser.setEmail(email);
        newUser.setFName(fName);
        newUser.setLName(lName);
        newUser.setAvtUrl(avtUrl);
        newUser.setPNum(pNum);
        newUser.setGender(gender);
        newUser.setDob(dob);
        newUser.setAccStatus(AccountStatus.ACTIVE);

        return newUser;
    }
    
    public Buyer toBuyerEntity() {
        return new Buyer(userId);
    }
}
