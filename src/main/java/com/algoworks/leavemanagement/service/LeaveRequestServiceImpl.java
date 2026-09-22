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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    @Autowired
    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository,
                                   EmployeeRepository employeeRepository,
                                   NotificationService notificationService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    @Override
    public LeaveResponseDTO applyLeave(Long employeeId, LeaveRequestDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with ID " + employeeId + " was not found"));

        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        LeaveType leaveType = (dto.getLeaveType() != null) ? dto.getLeaveType() : LeaveType.CASUAL;

        // Rule 1: Start date must not be after end date
        if (startDate.isAfter(endDate)) {
            throw new InvalidLeaveRequestException("Start date (" + startDate + ") cannot be after end date (" + endDate + ")");
        }

        long durationDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // Rule 2: Quota & Balance Check
        validateLeaveBalance(employee, leaveType, durationDays);

        // Rule 3: Check for overlapping active (PENDING or APPROVED) leave requests
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingActiveLeaves(employeeId, startDate, endDate);
        if (!overlapping.isEmpty()) {
            LeaveRequest conflict = overlapping.get(0);
            throw new InvalidLeaveRequestException(
                    "You already have a " + conflict.getStatus() + " leave request covering " +
                    conflict.getStartDate() + " to " + conflict.getEndDate());
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setStartDate(startDate);
        leaveRequest.setEndDate(endDate);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setReason(dto.getReason().trim());
        leaveRequest.setStatus(LeaveStatus.PENDING);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        // Emit simulated notification
        notificationService.sendNotification(
                "hr@organization.com",
                "HR Department",
                "New " + leaveType + " Leave Application",
                employee.getName() + " (" + employee.getDepartment() + ") applied for " + durationDays + " day(s) from " + startDate + " to " + endDate + ". Reason: " + dto.getReason(),
                "LEAVE_SUBMITTED"
        );

        return LeaveResponseDTO.fromEntity(saved);
    }

    private void validateLeaveBalance(Employee employee, LeaveType leaveType, long durationDays) {
        if (leaveType == LeaveType.CASUAL) {
            int available = (employee.getCasualLeaveBalance() != null) ? employee.getCasualLeaveBalance() : 0;
            if (available < durationDays) {
                throw new InvalidLeaveRequestException("Insufficient Casual Leave balance. Available: " + available + " day(s), Requested: " + durationDays + " day(s).");
            }
        } else if (leaveType == LeaveType.SICK) {
            int available = (employee.getSickLeaveBalance() != null) ? employee.getSickLeaveBalance() : 0;
            if (available < durationDays) {
                throw new InvalidLeaveRequestException("Insufficient Sick Leave balance. Available: " + available + " day(s), Requested: " + durationDays + " day(s).");
            }
        } else if (leaveType == LeaveType.ANNUAL) {
            int available = (employee.getAnnualLeaveBalance() != null) ? employee.getAnnualLeaveBalance() : 0;
            if (available < durationDays) {
                throw new InvalidLeaveRequestException("Insufficient Annual Leave balance. Available: " + available + " day(s), Requested: " + durationDays + " day(s).");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getAllLeaves() {
        return leaveRequestRepository.findAllByOrderByAppliedDateDesc()
                .stream()
                .map(LeaveResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getLeavesByEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }
        return leaveRequestRepository.findByEmployeeIdOrderByStartDateDesc(employeeId)
                .stream()
                .map(LeaveResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getLeavesByStatus(LeaveStatus status) {
        return leaveRequestRepository.findByStatusOrderByAppliedDateDesc(status)
                .stream()
                .map(LeaveResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getPendingLeaves() {
        return getLeavesByStatus(LeaveStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveResponseDTO getLeaveById(Long leaveId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request with ID " + leaveId + " was not found"));
        return LeaveResponseDTO.fromEntity(leave);
    }

    @Override
    public LeaveResponseDTO updateLeaveStatus(Long leaveId, LeaveStatus status) {
        return updateLeaveStatus(leaveId, status, null);
    }

    @Override
    public LeaveResponseDTO updateLeaveStatus(Long leaveId, LeaveStatus status, String managerRemarks) {
        if (status == null) {
            throw new InvalidLeaveRequestException("Status cannot be null");
        }

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request with ID " + leaveId + " was not found"));

        LeaveStatus oldStatus = leaveRequest.getStatus();
        Employee employee = leaveRequest.getEmployee();
        long duration = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        LeaveType type = leaveRequest.getLeaveType() != null ? leaveRequest.getLeaveType() : LeaveType.CASUAL;

        // Balance adjustment on status transition
        if (oldStatus != LeaveStatus.APPROVED && status == LeaveStatus.APPROVED) {
            deductBalance(employee, type, (int) duration);
            employeeRepository.save(employee);
        } else if (oldStatus == LeaveStatus.APPROVED && status != LeaveStatus.APPROVED) {
            restoreBalance(employee, type, (int) duration);
            employeeRepository.save(employee);
        }

        leaveRequest.setStatus(status);
        if (managerRemarks != null) {
            leaveRequest.setManagerRemarks(managerRemarks.trim());
        }

        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);

        // Emit simulated email notification to employee
        String remarksText = (managerRemarks != null && !managerRemarks.isBlank()) ? " Remarks: " + managerRemarks.trim() : "";
        notificationService.sendNotification(
                employee.getEmail(),
                employee.getName(),
                "Leave Request #" + leaveId + " " + status,
                "Your " + type + " leave from " + leaveRequest.getStartDate() + " to " + leaveRequest.getEndDate() + " (" + duration + " days) has been " + status + "." + remarksText,
                status == LeaveStatus.APPROVED ? "LEAVE_APPROVED" : "LEAVE_REJECTED"
        );

        return LeaveResponseDTO.fromEntity(updated);
    }

    private void deductBalance(Employee employee, LeaveType leaveType, int days) {
        if (leaveType == LeaveType.CASUAL) {
            int current = (employee.getCasualLeaveBalance() != null) ? employee.getCasualLeaveBalance() : 0;
            employee.setCasualLeaveBalance(Math.max(0, current - days));
        } else if (leaveType == LeaveType.SICK) {
            int current = (employee.getSickLeaveBalance() != null) ? employee.getSickLeaveBalance() : 0;
            employee.setSickLeaveBalance(Math.max(0, current - days));
        } else if (leaveType == LeaveType.ANNUAL) {
            int current = (employee.getAnnualLeaveBalance() != null) ? employee.getAnnualLeaveBalance() : 0;
            employee.setAnnualLeaveBalance(Math.max(0, current - days));
        }
    }

    private void restoreBalance(Employee employee, LeaveType leaveType, int days) {
        if (leaveType == LeaveType.CASUAL) {
            int current = (employee.getCasualLeaveBalance() != null) ? employee.getCasualLeaveBalance() : 0;
            employee.setCasualLeaveBalance(current + days);
        } else if (leaveType == LeaveType.SICK) {
            int current = (employee.getSickLeaveBalance() != null) ? employee.getSickLeaveBalance() : 0;
            employee.setSickLeaveBalance(current + days);
        } else if (leaveType == LeaveType.ANNUAL) {
            int current = (employee.getAnnualLeaveBalance() != null) ? employee.getAnnualLeaveBalance() : 0;
            employee.setAnnualLeaveBalance(current + days);
        }
    }

    @Override
    public void deleteLeave(Long leaveId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request with ID " + leaveId + " was not found"));

        if (leave.getStatus() == LeaveStatus.APPROVED) {
            Employee employee = leave.getEmployee();
            long duration = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
            restoreBalance(employee, leave.getLeaveType(), (int) duration);
            employeeRepository.save(employee);
        }

        leaveRequestRepository.delete(leave);
    }

    @Override
    @Transactional(readOnly = true)
    public String exportLeavesToCsv() {
        List<LeaveRequest> list = leaveRequestRepository.findAllByOrderByAppliedDateDesc();
        StringBuilder sb = new StringBuilder();
        sb.append("Request ID,Employee ID,Employee Name,Employee Email,Department,Leave Type,Start Date,End Date,Duration (Days),Status,Manager Remarks,Reason,Applied Date\n");

        for (LeaveRequest l : list) {
            long days = (l.getStartDate() != null && l.getEndDate() != null)
                    ? ChronoUnit.DAYS.between(l.getStartDate(), l.getEndDate()) + 1
                    : 0;

            sb.append(l.getId()).append(",")
                    .append(l.getEmployee() != null ? l.getEmployee().getId() : "").append(",")
                    .append("\"").append(escapeCsv(l.getEmployee() != null ? l.getEmployee().getName() : "")).append("\",")
                    .append("\"").append(escapeCsv(l.getEmployee() != null ? l.getEmployee().getEmail() : "")).append("\",")
                    .append("\"").append(escapeCsv(l.getEmployee() != null ? l.getEmployee().getDepartment() : "")).append("\",")
                    .append(l.getLeaveType() != null ? l.getLeaveType().name() : "CASUAL").append(",")
                    .append(l.getStartDate()).append(",")
                    .append(l.getEndDate()).append(",")
                    .append(days).append(",")
                    .append(l.getStatus()).append(",")
                    .append("\"").append(escapeCsv(l.getManagerRemarks() != null ? l.getManagerRemarks() : "")).append("\",")
                    .append("\"").append(escapeCsv(l.getReason())).append("\",")
                    .append(l.getAppliedDate()).append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        return input.replace("\"", "\"\"");
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveAnalyticsDTO getLeaveAnalytics() {
        long totalEmp = employeeRepository.count();
        List<LeaveRequest> allLeaves = leaveRequestRepository.findAll();
        long totalLeaves = allLeaves.size();
        long pending = allLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count();
        long approved = allLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.APPROVED).count();
        long rejected = allLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.REJECTED).count();

        double approvalRate = (totalLeaves > 0) ? (double) approved / totalLeaves * 100.0 : 0.0;
        approvalRate = Math.round(approvalRate * 10.0) / 10.0;

        Map<String, Long> byDept = allLeaves.stream()
                .filter(l -> l.getEmployee() != null && l.getEmployee().getDepartment() != null)
                .collect(Collectors.groupingBy(l -> l.getEmployee().getDepartment(), Collectors.counting()));

        Map<String, Long> byType = allLeaves.stream()
                .collect(Collectors.groupingBy(l -> l.getLeaveType() != null ? l.getLeaveType().name() : "CASUAL", Collectors.counting()));

        return new LeaveAnalyticsDTO(totalEmp, totalLeaves, pending, approved, rejected, approvalRate, byDept, byType);
    }
}
