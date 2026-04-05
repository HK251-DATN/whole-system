package edu.hcmut.datn.back_office_service.dto.request;

import edu.hcmut.datn.back_office_service.common.enums.EmployeeStatus;
import edu.hcmut.datn.back_office_service.dao.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateRequest {

    private EmployeeStatus empStatus;

    public Employee toEntity() {
        return new Employee(empStatus);
    }
}
