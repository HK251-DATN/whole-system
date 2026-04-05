package edu.hcmut.datn.back_office_service.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;

import edu.hcmut.datn.back_office_service.common.enums.AccountStatus;
import edu.hcmut.datn.back_office_service.common.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@NoArgsConstructor
@ToString
public class User {

    @Column(name = "user_id")
    @Id
    @Setter
    @Getter
    private Long userId;

    @Column(name = "email")
    @Setter
    @Getter
    private String email;

    @Column(name = "f_name")
    @Setter
    @Getter
    private String fName;

    @Column(name = "l_name")
    @Setter
    @Getter
    private String lName;

    @Column(name = "avt_url")
    @Setter
    @Getter
    private String avtUrl;

    @Column(name = "dob")
    @Setter
    @Getter
    private LocalDate dob;

    @Column(name = "p_num")
    @Setter
    @Getter
    private String pNum;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "gender")
    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "acc_status")
    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private AccountStatus accStatus;

    public User(Long userId, String email, String fName, String lName, String avtUrl, LocalDate dob, String pNum,
            Gender gender, AccountStatus accStatus) {
        this.userId = userId;
        this.email = email;
        this.fName = fName;
        this.lName = lName;
        this.avtUrl = avtUrl;
        this.dob = dob;
        this.pNum = pNum;
        this.gender = gender;
        this.accStatus = accStatus;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(); // Set createdAt on first save
        updatedAt = LocalDateTime.now(); // Optional: Set initial updatedAt
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(); // Update on every save after creation
    }
}
