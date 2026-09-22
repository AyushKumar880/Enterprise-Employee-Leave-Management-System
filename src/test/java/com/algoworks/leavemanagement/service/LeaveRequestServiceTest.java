package com.algoworks.leavemanagement.service;

import com.algoworks.leavemanagement.dto.LeaveAnalyticsDTO;
import com.algoworks.leavemanagement.dto.LeaveRequestDTO;
import com.algoworks.leavemanagement.dto.LeaveResponseDTO;
import com.algoworks.leavemanagement.entity.Employee;
import com.algoworks.leavemanagement.entity.LeaveRequest;
import com.algoworks.leavemanagement.entity.LeaveStatus;
import com.algoworks.leavemanagement.entity.LeaveType;
import com.algoworks.leavemanagement.exception.InvalidLeaveRequestException;
import com.algoworks.leavemanagement.exception.ResourceNotFoundException;
import com.algoworks.leavemanagement.repository.EmployeeRepository;
import com.algoworks.leavemanagement.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LeaveRequestServiceImpl leaveRequestService;

    private Employee employee;
    private LeaveRequest leaveRequest;
    private LeaveRequestDTO leaveDTO;

    @BeforeEach
    void setUp() {
        employee = new Employee(1L, "Aliva Sharma", "aliva@example.com", "IT", "Graduate Trainee", 12, 10, 15);
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(5);
        leaveRequest = new LeaveRequest(1L, employee, start, end, "Personal work", LeaveStatus.PENDING, LeaveType.CASUAL);
        leaveDTO = new LeaveRequestDTO(start, end, LeaveType.CASUAL, "Personal work");
    }

    @Test
    @DisplayName("Should successfully apply for leave when valid dates provided")
    void testApplyLeave_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.findOverlappingActiveLeaves(eq(1L), any(), any())).thenReturn(Collections.emptyList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        doNothing().when(notificationService).sendNotification(anyString(), anyString(), anyString(), anyString(), anyString());

        LeaveResponseDTO result = leaveRequestService.applyLeave(1L, leaveDTO);

        assertNotNull(result);
        assertEquals(LeaveStatus.PENDING, result.getStatus());
        assertEquals("Personal work", result.getReason());
        assertEquals(LeaveType.CASUAL, result.getLeaveType());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
        verify(notificationService, times(1)).sendNotification(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidLeaveRequestException when start date is after end date")
    void testApplyLeave_InvalidDateRange() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        LeaveRequestDTO invalidDTO = new LeaveRequestDTO(
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(5),
                LeaveType.CASUAL,
                "Invalid dates"
        );

        assertThrows(InvalidLeaveRequestException.class, () -> leaveRequestService.applyLeave(1L, invalidDTO));
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Should throw InvalidLeaveRequestException when overlapping active leave exists")
    void testApplyLeave_OverlappingLeave() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.findOverlappingActiveLeaves(eq(1L), any(), any()))
                .thenReturn(List.of(leaveRequest));

        assertThrows(InvalidLeaveRequestException.class, () -> leaveRequestService.applyLeave(1L, leaveDTO));
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when applying leave for non-existent employee")
    void testApplyLeave_EmployeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> leaveRequestService.applyLeave(99L, leaveDTO));
    }

    @Test
    @DisplayName("Should update leave status from PENDING to APPROVED and deduct balance")
    void testUpdateLeaveStatus_Approve() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(notificationService).sendNotification(anyString(), anyString(), anyString(), anyString(), anyString());

        LeaveResponseDTO result = leaveRequestService.updateLeaveStatus(1L, LeaveStatus.APPROVED, "Approved by Manager");

        assertNotNull(result);
        assertEquals(LeaveStatus.APPROVED, result.getStatus());
        assertEquals("Approved by Manager", result.getManagerRemarks());
        verify(leaveRequestRepository, times(1)).save(leaveRequest);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    @DisplayName("Should export leaves to valid CSV string")
    void testExportLeavesToCsv() {
        when(leaveRequestRepository.findAllByOrderByAppliedDateDesc()).thenReturn(List.of(leaveRequest));

        String csv = leaveRequestService.exportLeavesToCsv();

        assertNotNull(csv);
        assertTrue(csv.contains("Request ID,Employee ID,Employee Name"));
        assertTrue(csv.contains("Aliva Sharma"));
    }

    @Test
    @DisplayName("Should compute correct leave analytics summary")
    void testGetLeaveAnalytics() {
        when(employeeRepository.count()).thenReturn(1L);
        when(leaveRequestRepository.findAll()).thenReturn(List.of(leaveRequest));

        LeaveAnalyticsDTO analytics = leaveRequestService.getLeaveAnalytics();

        assertNotNull(analytics);
        assertEquals(1, analytics.getTotalEmployees());
        assertEquals(1, analytics.getTotalLeaveRequests());
        assertEquals(1, analytics.getPendingRequests());
    }
}
