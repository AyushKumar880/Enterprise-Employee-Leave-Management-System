package com.algoworks.leavemanagement.service;

import com.algoworks.leavemanagement.dto.EmployeeRequestDTO;
import com.algoworks.leavemanagement.dto.EmployeeResponseDTO;
import com.algoworks.leavemanagement.entity.Employee;
import com.algoworks.leavemanagement.exception.DuplicateResourceException;
import com.algoworks.leavemanagement.exception.ResourceNotFoundException;
import com.algoworks.leavemanagement.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO) {
        if (employeeRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException(
                    "An employee with email '" + requestDTO.getEmail() + "' already exists");
        }

        Employee employee = new Employee();
        employee.setName(requestDTO.getName().trim());
        employee.setEmail(requestDTO.getEmail().trim().toLowerCase());
        employee.setDepartment(requestDTO.getDepartment().trim());
        employee.setDesignation(requestDTO.getDesignation().trim());
        employee.setCasualLeaveBalance(12);
        employee.setSickLeaveBalance(10);
        employee.setAnnualLeaveBalance(15);

        Employee saved = employeeRepository.save(employee);
        return EmployeeResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAllEmployees(String department) {
        List<Employee> list;
        if (department != null && !department.trim().isEmpty()) {
            list = employeeRepository.findByDepartmentIgnoreCase(department.trim());
        } else {
            list = employeeRepository.findAll();
        }
        return list.stream()
                .map(EmployeeResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = getEmployeeEntityById(id);
        return EmployeeResponseDTO.fromEntity(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeEntityById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with ID " + id + " was not found"));
    }

    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO) {
        Employee employee = getEmployeeEntityById(id);

        String newEmail = requestDTO.getEmail().trim().toLowerCase();
        if (!employee.getEmail().equalsIgnoreCase(newEmail) && employeeRepository.existsByEmail(newEmail)) {
            throw new DuplicateResourceException("An employee with email '" + newEmail + "' already exists");
        }

        employee.setName(requestDTO.getName().trim());
        employee.setEmail(newEmail);
        employee.setDepartment(requestDTO.getDepartment().trim());
        employee.setDesignation(requestDTO.getDesignation().trim());

        Employee updated = employeeRepository.save(employee);
        return EmployeeResponseDTO.fromEntity(updated);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeEntityById(id);
        employeeRepository.delete(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalEmployeeCount() {
        return employeeRepository.count();
    }
}
