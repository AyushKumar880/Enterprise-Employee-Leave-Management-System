package com.algoworks.leavemanagement.dto;

import com.algoworks.leavemanagement.entity.LeaveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload for updating the status of a leave request")
public class LeaveStatusUpdateDTO {

    @NotNull(message = "Status is required")
    @Schema(description = "New status for the leave request (APPROVED, REJECTED, or PENDING)", example = "APPROVED")
    private LeaveStatus status;

    @Schema(description = "Optional manager remarks or reason for status decision", example = "Approved, please complete project handoff.")
    private String managerRemarks;

    public LeaveStatusUpdateDTO() {
    }

    public LeaveStatusUpdateDTO(LeaveStatus status) {
        this.status = status;
    }

    public LeaveStatusUpdateDTO(LeaveStatus status, String managerRemarks) {
        this.status = status;
        this.managerRemarks = managerRemarks;
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
}
