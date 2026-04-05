package edu.hcmut.datn.back_office_service.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.Employee;
import edu.hcmut.datn.back_office_service.exception.employee.EmployeeAlreadyExistsException;
import edu.hcmut.datn.back_office_service.exception.employee.EmployeeNotFoundException;
import edu.hcmut.datn.back_office_service.repository.EmployeeRepository;
import edu.hcmut.datn.back_office_service.service.EmployeeService;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee create(Employee employee) {
        if (employee.getUserId() != null && employeeRepository.existsByUserId(employee.getUserId())) {
            throw new EmployeeAlreadyExistsException("Employee with given userId already exists");
        }

        return employeeRepository.save(employee);
    }

    @Override
    public Employee read(Long employeeId) {
        return employeeRepository.findById(employeeId).orElseThrow(() -> new EmployeeNotFoundException("Employee Not Found"));
    }

    @Override
    public List<Employee> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<Employee> page = employeeRepository.findAll(pageable);

        return page.toList();
    }

    @Override
    public Employee update(Long employeeId, Employee employee) {
        Employee curEmp = read(employeeId);

        curEmp.setEmpStatus(employee.getEmpStatus());

        return employeeRepository.save(curEmp);
    }

    @Override
    public void delete(Long employeeId) {
        employeeRepository.delete(read(employeeId));
    }
}
