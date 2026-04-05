package edu.hcmut.datn.back_office_service.dto.request;

import java.time.LocalDate;

import edu.hcmut.datn.back_office_service.common.enums.AccountStatus;
import edu.hcmut.datn.back_office_service.common.enums.Gender;
import edu.hcmut.datn.back_office_service.dao.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;

    private String email;

    private String fName;

    private String lName;

    private String avtUrl;

    private LocalDate dob;

    private String pNum;

    private Gender gender;

    private AccountStatus accStatus;

    public User toEntity() {
        return new User(userId, email, fName, lName, avtUrl, dob, pNum, gender, accStatus);
    }
}
