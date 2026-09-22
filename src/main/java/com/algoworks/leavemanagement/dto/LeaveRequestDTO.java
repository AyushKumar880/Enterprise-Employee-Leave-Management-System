package com.algoworks.leavemanagement.dto;

import com.algoworks.leavemanagement.entity.LeaveType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Payload sent by an employee when applying for a leave.
 */
@Schema(description = "Request payload for applying for a leave")
public class LeaveRequestDTO {

    @Schema(description = "Employee ID (required if applying via POST /api/leaves)", example = "1")
    private Long employeeId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Start date of leave (YYYY-MM-DD)", example = "2026-09-10")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "End date of leave (YYYY-MM-DD)", example = "2026-09-12")
    private LocalDate endDate;

    @Schema(description = "Type of leave (CASUAL, SICK, ANNUAL, EMERGENCY)", example = "CASUAL")
    private LeaveType leaveType = LeaveType.CASUAL;

    @NotBlank(message = "Reason is required")
    @Size(min = 3, max = 500, message = "Reason must be between 3 and 500 characters")
    @Schema(description = "Reason for leave request", example = "Personal work")
    private String reason;

    public LeaveRequestDTO() {
    }

    public LeaveRequestDTO(LocalDate startDate, LocalDate endDate, String reason) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.leaveType = LeaveType.CASUAL;
    }

    public LeaveRequestDTO(LocalDate startDate, LocalDate endDate, LeaveType leaveType, String reason) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveType = (leaveType != null) ? leaveType : LeaveType.CASUAL;
        this.reason = reason;
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

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = (leaveType != null) ? leaveType : LeaveType.CASUAL;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
