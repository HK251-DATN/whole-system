package edu.hcmut.datn.back_office_service.dto.request;

import java.time.LocalDate;

import edu.hcmut.datn.back_office_service.common.enums.EmployeeStatus;
import edu.hcmut.datn.back_office_service.dao.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateRequest {

    private LocalDate hireDate;
    private Long userId;
    private EmployeeStatus empStatus;

    public Employee toEntity() {
        return new Employee(hireDate, userId, empStatus);
    }
}
