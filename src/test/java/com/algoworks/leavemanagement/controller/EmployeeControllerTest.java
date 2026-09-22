package com.algoworks.leavemanagement.controller;

import com.algoworks.leavemanagement.dto.EmployeeRequestDTO;
import com.algoworks.leavemanagement.dto.EmployeeResponseDTO;
import com.algoworks.leavemanagement.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("POST /api/employees - Should create employee successfully (201 Created)")
    void testCreateEmployee() throws Exception {
        EmployeeRequestDTO request = new EmployeeRequestDTO("Aliva", "aliva@example.com", "IT", "Graduate Trainee");
        EmployeeResponseDTO response = new EmployeeResponseDTO(1L, "Aliva", "aliva@example.com", "IT", "Graduate Trainee", LocalDateTime.now(), 0);

        when(employeeService.createEmployee(any(EmployeeRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Aliva"));
    }

    @Test
    @DisplayName("GET /api/employees - Should return list of employees (200 OK)")
    void testGetAllEmployees() throws Exception {
        EmployeeResponseDTO response = new EmployeeResponseDTO(1L, "Aliva", "aliva@example.com", "IT", "Graduate Trainee", LocalDateTime.now(), 0);
        when(employeeService.getAllEmployees(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Aliva"));
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Should return employee by ID")
    void testGetEmployeeById() throws Exception {
        EmployeeResponseDTO response = new EmployeeResponseDTO(1L, "Aliva", "aliva@example.com", "IT", "Graduate Trainee", LocalDateTime.now(), 0);
        when(employeeService.getEmployeeById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}
