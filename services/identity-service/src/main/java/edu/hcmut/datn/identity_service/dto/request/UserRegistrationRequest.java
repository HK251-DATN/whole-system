package edu.hcmut.datn.identity_service.dto.request;

import java.time.LocalDate;

import edu.hcmut.datn.identity_service.common.enums.Gender;
import lombok.Getter;

public class UserRegistrationRequest {

    @Getter
    private String email;

    @Getter
    private String password;

    @Getter
    private String fName;

    @Getter
    private String lName;

    @Getter
    private LocalDate dob;

    @Getter
    private String pNum;

    @Getter
    private Gender gender;

    public UserRequest toUserRequest() {
        return new UserRequest(email, password);
    }
}
