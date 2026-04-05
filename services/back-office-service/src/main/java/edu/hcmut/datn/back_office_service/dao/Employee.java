package edu.hcmut.datn.back_office_service.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;

import edu.hcmut.datn.back_office_service.common.enums.EmployeeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employees")
@NoArgsConstructor
public class Employee {

    @Column(name = "emp_id")
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long empId;

    @Column(name = "hire_date")
    @Getter
    private LocalDate hireDate;

    @Column(name = "user_id")
    @Getter
    private Long userId;

    @Column(name = "emp_status")
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private EmployeeStatus empStatus;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Contructor for creating purpose
    public Employee(LocalDate hireDate, Long userId, EmployeeStatus empStatus) {
        this.hireDate = hireDate;
        this.userId = userId;
        this.empStatus = empStatus;
    }

    // Contructor for updateing purpose
    public Employee(EmployeeStatus empStatus) {
        this.empStatus = empStatus;
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
