package edu.hcmut.datn.back_office_service.service;

import java.util.List;

import edu.hcmut.datn.back_office_service.dao.Employee;

public interface EmployeeService {

    Employee create(Employee employee);

    Employee read(Long employeeId);

    List<Employee> readAll(Integer pageNum, Integer pageSize);

    Employee update(Long employeeId, Employee employee);

    void delete(Long employeeId);
}
