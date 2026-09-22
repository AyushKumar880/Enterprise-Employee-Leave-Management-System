package com.algoworks.leavemanagement.service;

import com.algoworks.leavemanagement.dto.EmployeeRequestDTO;
import com.algoworks.leavemanagement.dto.EmployeeResponseDTO;
import com.algoworks.leavemanagement.entity.Employee;

import java.util.List;

public interface EmployeeService {

    EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO);

    List<EmployeeResponseDTO> getAllEmployees(String department);

    EmployeeResponseDTO getEmployeeById(Long id);

    Employee getEmployeeEntityById(Long id);

    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO);

    void deleteEmployee(Long id);

    long getTotalEmployeeCount();
}
