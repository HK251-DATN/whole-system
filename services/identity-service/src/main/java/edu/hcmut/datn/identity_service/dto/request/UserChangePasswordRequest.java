package edu.hcmut.datn.identity_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordRequest {
    String oldPassword;
    String newPassword;
}
