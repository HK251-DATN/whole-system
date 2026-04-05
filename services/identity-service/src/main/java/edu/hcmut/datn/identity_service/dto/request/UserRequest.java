package edu.hcmut.datn.identity_service.dto.request;

import edu.hcmut.datn.identity_service.dao.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private String email;

    private String password;

    public User toEntity() {
        User user = new User();

        user.setUserEmail(this.email);
        user.setHashedPwd(this.password);

        return user;
    }
}
