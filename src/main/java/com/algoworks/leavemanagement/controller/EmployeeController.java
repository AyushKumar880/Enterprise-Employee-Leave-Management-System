package com.algoworks.leavemanagement.controller;

import com.algoworks.leavemanagement.dto.ApiResponse;
import com.algoworks.leavemanagement.dto.EmployeeRequestDTO;
import com.algoworks.leavemanagement.dto.EmployeeResponseDTO;
import com.algoworks.leavemanagement.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
@Tag(name = "Employee Management", description = "Endpoints for managing employee records (CRUD)")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @Operation(summary = "Create a new employee", description = "Registers a new employee into the system with unique email.")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> createEmployee(
            @Valid @RequestBody EmployeeRequestDTO requestDTO) {
        EmployeeResponseDTO created = employeeService.createEmployee(requestDTO);
        return new ResponseEntity<>(
                ApiResponse.success("Employee created successfully", created),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(summary = "Get all employees", description = "Retrieves all employees, optionally filtered by department.")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDTO>>> getAllEmployees(
            @Parameter(description = "Filter employees by department (e.g. IT, HR)")
            @RequestParam(required = false) String department) {
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees(department);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Retrieves specific employee details by their unique ID.")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> getEmployeeById(
            @PathVariable Long id) {
        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee found", employee));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee details", description = "Updates name, email, department, or designation of an existing employee.")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO requestDTO) {
        EmployeeResponseDTO updated = employeeService.updateEmployee(id, requestDTO);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee", description = "Permanently removes an employee and their associated leave records.")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully", null));
    }
}
