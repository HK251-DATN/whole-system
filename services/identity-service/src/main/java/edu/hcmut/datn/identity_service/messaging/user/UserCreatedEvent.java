package edu.hcmut.datn.identity_service.messaging.user;

import java.time.LocalDate;

import edu.hcmut.datn.identity_service.common.enums.Gender;

public record UserCreatedEvent(
    Long userId,
    String email,
    String fName,
    String lName,
    String avtUrl,
    LocalDate dob,
    String pNum,
    Gender gender
) {}
