package edu.hcmut.datn.back_office_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.back_office_service.dao.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Boolean existsByUserId(Long userId);
}
