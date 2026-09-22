package com.algoworks.leavemanagement.service;

import com.algoworks.leavemanagement.dto.EmployeeRequestDTO;
import com.algoworks.leavemanagement.dto.EmployeeResponseDTO;
import com.algoworks.leavemanagement.entity.Employee;
import com.algoworks.leavemanagement.exception.DuplicateResourceException;
import com.algoworks.leavemanagement.exception.ResourceNotFoundException;
import com.algoworks.leavemanagement.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;
    private EmployeeRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        employee = new Employee(1L, "Aliva Sharma", "aliva@example.com", "IT", "Graduate Trainee", new ArrayList<>());
        requestDTO = new EmployeeRequestDTO("Aliva Sharma", "aliva@example.com", "IT", "Graduate Trainee");
    }

    @Test
    @DisplayName("Should successfully create a new employee when email is unique")
    void testCreateEmployee_Success() {
        when(employeeRepository.existsByEmail("aliva@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponseDTO result = employeeService.createEmployee(requestDTO);

        assertNotNull(result);
        assertEquals("Aliva Sharma", result.getName());
        assertEquals("aliva@example.com", result.getEmail());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating employee with already registered email")
    void testCreateEmployee_DuplicateEmail() {
        when(employeeRepository.existsByEmail("aliva@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> employeeService.createEmployee(requestDTO));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should retrieve employee by ID successfully")
    void testGetEmployeeById_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeResponseDTO result = employeeService.getEmployeeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Aliva Sharma", result.getName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when employee ID does not exist")
    void testGetEmployeeById_NotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    @DisplayName("Should retrieve all employees")
    void testGetAllEmployees() {
        Employee employee2 = new Employee(2L, "Priya Patel", "priya@example.com", "HR", "HR Exec", new ArrayList<>());
        when(employeeRepository.findAll()).thenReturn(List.of(employee, employee2));

        List<EmployeeResponseDTO> result = employeeService.getAllEmployees(null);

        assertEquals(2, result.size());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should delete employee when ID exists")
    void testDeleteEmployee_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).delete(employee);

        assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));
        verify(employeeRepository, times(1)).delete(employee);
    }
}
