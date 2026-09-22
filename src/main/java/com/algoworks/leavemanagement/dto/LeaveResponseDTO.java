package com.algoworks.leavemanagement.dto;

import com.algoworks.leavemanagement.entity.LeaveRequest;
import com.algoworks.leavemanagement.entity.LeaveStatus;
import com.algoworks.leavemanagement.entity.LeaveType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Schema(description = "Response representation of a leave request")
public class LeaveResponseDTO {

    @Schema(description = "Unique leave request ID", example = "1")
    private Long id;

    @Schema(description = "ID of the employee who requested leave", example = "1")
    private Long employeeId;

    @Schema(description = "Name of the employee", example = "Aliva")
    private String employeeName;

    @Schema(description = "Email of the employee", example = "aliva@example.com")
    private String employeeEmail;

    @Schema(description = "Department of the employee", example = "IT")
    private String employeeDepartment;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Leave start date", example = "2026-09-10")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Leave end date", example = "2026-09-12")
    private LocalDate endDate;

    @Schema(description = "Duration in days", example = "3")
    private long durationDays;

    @Schema(description = "Type of leave", example = "CASUAL")
    private LeaveType leaveType;

    @Schema(description = "Reason for leave", example = "Personal work")
    private String reason;

    @Schema(description = "Current status of the leave request", example = "PENDING")
    private LeaveStatus status;

    @Schema(description = "Optional manager remarks on approval or rejection", example = "Approved, please hand over pending tasks.")
    private String managerRemarks;

    @Schema(description = "Timestamp when leave was submitted")
    private LocalDateTime appliedDate;

    @Schema(description = "Timestamp when leave status was last updated")
    private LocalDateTime updatedDate;

    public LeaveResponseDTO() {
    }

    public LeaveResponseDTO(Long id, Long employeeId, String employeeName, String employeeEmail, String employeeDepartment,
                            LocalDate startDate, LocalDate endDate, long durationDays, LeaveType leaveType, String reason,
                            LeaveStatus status, String managerRemarks, LocalDateTime appliedDate, LocalDateTime updatedDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.employeeDepartment = employeeDepartment;
        this.startDate = startDate;
        this.endDate = endDate;
        this.durationDays = durationDays;
        this.leaveType = (leaveType != null) ? leaveType : LeaveType.CASUAL;
        this.reason = reason;
        this.status = status;
        this.managerRemarks = managerRemarks;
        this.appliedDate = appliedDate;
        this.updatedDate = updatedDate;
    }

    public static LeaveResponseDTO fromEntity(LeaveRequest leave) {
        if (leave == null) {
            return null;
        }
        Long empId = (leave.getEmployee() != null) ? leave.getEmployee().getId() : null;
        String empName = (leave.getEmployee() != null) ? leave.getEmployee().getName() : null;
        String empEmail = (leave.getEmployee() != null) ? leave.getEmployee().getEmail() : null;
        String empDept = (leave.getEmployee() != null) ? leave.getEmployee().getDepartment() : null;

        long days = 0;
        if (leave.getStartDate() != null && leave.getEndDate() != null) {
            days = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
        }

        return new LeaveResponseDTO(
                leave.getId(),
                empId,
                empName,
                empEmail,
                empDept,
                leave.getStartDate(),
                leave.getEndDate(),
                days,
                leave.getLeaveType(),
                leave.getReason(),
                leave.getStatus(),
                leave.getManagerRemarks(),
                leave.getAppliedDate(),
                leave.getUpdatedDate()
        );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getEmployeeDepartment() {
        return employeeDepartment;
    }

    public void setEmployeeDepartment(String employeeDepartment) {
        this.employeeDepartment = employeeDepartment;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public long getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(long durationDays) {
        this.durationDays = durationDays;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public String getManagerRemarks() {
        return managerRemarks;
    }

    public void setManagerRemarks(String managerRemarks) {
        this.managerRemarks = managerRemarks;
    }

    public LocalDateTime getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(LocalDateTime appliedDate) {
        this.appliedDate = appliedDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
}
