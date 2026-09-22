package com.algoworks.leavemanagement.controller;

import com.algoworks.leavemanagement.dto.LeaveAnalyticsDTO;
import com.algoworks.leavemanagement.dto.LeaveRequestDTO;
import com.algoworks.leavemanagement.dto.LeaveResponseDTO;
import com.algoworks.leavemanagement.entity.LeaveStatus;
import com.algoworks.leavemanagement.entity.LeaveType;
import com.algoworks.leavemanagement.service.LeaveRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaveRequestController.class)
class LeaveRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LeaveRequestService leaveRequestService;

    @Test
    @DisplayName("POST /api/leaves/employee/{id} - Should submit leave request (201 Created)")
    void testApplyLeave() throws Exception {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);
        LeaveRequestDTO request = new LeaveRequestDTO(start, end, LeaveType.CASUAL, "Personal work");

        LeaveResponseDTO response = new LeaveResponseDTO(
                1L, 1L, "Aliva Sharma", "aliva@example.com", "IT",
                start, end, 3, LeaveType.CASUAL, "Personal work", LeaveStatus.PENDING, null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(leaveRequestService.applyLeave(eq(1L), any(LeaveRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/leaves/employee/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.leaveType").value("CASUAL"));
    }

    @Test
    @DisplayName("POST /api/leaves - Should submit leave request with employeeId in body (201 Created)")
    void testApplyLeaveStandard() throws Exception {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);
        LeaveRequestDTO request = new LeaveRequestDTO(start, end, LeaveType.CASUAL, "Personal work");
        request.setEmployeeId(1L);

        LeaveResponseDTO response = new LeaveResponseDTO(
                1L, 1L, "Aliva Sharma", "aliva@example.com", "IT",
                start, end, 3, LeaveType.CASUAL, "Personal work", LeaveStatus.PENDING, null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(leaveRequestService.applyLeave(eq(1L), any(LeaveRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("GET /api/leaves/pending - Should return list of pending leaves")
    void testGetPendingLeaves() throws Exception {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);
        LeaveResponseDTO response = new LeaveResponseDTO(
                1L, 1L, "Aliva", "aliva@example.com", "IT",
                start, end, 3, LeaveType.CASUAL, "Personal work", LeaveStatus.PENDING, null, LocalDateTime.now(), LocalDateTime.now()
        );

        when(leaveRequestService.getPendingLeaves()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/leaves/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("PATCH /api/leaves/{id}/status - Should update status to APPROVED with remarks")
    void testUpdateLeaveStatus() throws Exception {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);
        LeaveResponseDTO response = new LeaveResponseDTO(
                1L, 1L, "Aliva", "aliva@example.com", "IT",
                start, end, 3, LeaveType.CASUAL, "Personal work", LeaveStatus.APPROVED, "Approved by admin", LocalDateTime.now(), LocalDateTime.now()
        );

        when(leaveRequestService.updateLeaveStatus(eq(1L), eq(LeaveStatus.APPROVED), eq("Approved by admin"))).thenReturn(response);

        mockMvc.perform(patch("/api/leaves/1/status?status=APPROVED&remarks=Approved by admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("GET /api/leaves/analytics - Should return company leave analytics")
    void testGetLeaveAnalytics() throws Exception {
        LeaveAnalyticsDTO analytics = new LeaveAnalyticsDTO(
                5, 10, 2, 7, 1, 70.0,
                Map.of("IT Engineering", 5L),
                Map.of("CASUAL", 6L, "SICK", 4L)
        );

        when(leaveRequestService.getLeaveAnalytics()).thenReturn(analytics);

        mockMvc.perform(get("/api/leaves/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalEmployees").value(5))
                .andExpect(jsonPath("$.data.approvalRate").value(70.0));
    }

    @Test
    @DisplayName("GET /api/leaves/export - Should return CSV attachment")
    void testExportLeavesToCsv() throws Exception {
        String csv = "Request ID,Employee ID\n1,1\n";
        when(leaveRequestService.exportLeavesToCsv()).thenReturn(csv);

        mockMvc.perform(get("/api/leaves/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"leave-records.csv\""))
                .andExpect(content().string(csv));
    }
}
