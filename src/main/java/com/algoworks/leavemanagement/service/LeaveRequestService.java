package com.algoworks.leavemanagement.service;

import com.algoworks.leavemanagement.dto.LeaveAnalyticsDTO;
import com.algoworks.leavemanagement.dto.LeaveRequestDTO;
import com.algoworks.leavemanagement.dto.LeaveResponseDTO;
import com.algoworks.leavemanagement.entity.LeaveStatus;

import java.util.List;

public interface LeaveRequestService {

    LeaveResponseDTO applyLeave(Long employeeId, LeaveRequestDTO dto);

    List<LeaveResponseDTO> getAllLeaves();

    List<LeaveResponseDTO> getLeavesByEmployee(Long employeeId);

    List<LeaveResponseDTO> getLeavesByStatus(LeaveStatus status);

    List<LeaveResponseDTO> getPendingLeaves();

    LeaveResponseDTO getLeaveById(Long leaveId);

    LeaveResponseDTO updateLeaveStatus(Long leaveId, LeaveStatus status);

    LeaveResponseDTO updateLeaveStatus(Long leaveId, LeaveStatus status, String managerRemarks);

    void deleteLeave(Long leaveId);

    String exportLeavesToCsv();

    LeaveAnalyticsDTO getLeaveAnalytics();
}
