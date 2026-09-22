package com.algoworks.leavemanagement.controller;

import com.algoworks.leavemanagement.dto.ApiResponse;
import com.algoworks.leavemanagement.dto.LeaveAnalyticsDTO;
import com.algoworks.leavemanagement.dto.LeaveRequestDTO;
import com.algoworks.leavemanagement.dto.LeaveResponseDTO;
import com.algoworks.leavemanagement.dto.LeaveStatusUpdateDTO;
import com.algoworks.leavemanagement.entity.LeaveStatus;
import com.algoworks.leavemanagement.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(origins = "*")
@Tag(name = "Leave Management", description = "Endpoints for leave requests, approvals, employee history, CSV export, and analytics")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @Autowired
    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping
    @Operation(summary = "Apply for leave (standard endpoint)", description = "Allows an employee to submit a leave request by providing employeeId in the request body.")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> applyLeaveStandard(
            @Valid @RequestBody LeaveRequestDTO dto) {
        if (dto.getEmployeeId() == null) {
            throw new IllegalArgumentException("employeeId is required in request body when applying via /api/leaves");
        }
        LeaveResponseDTO response = leaveRequestService.applyLeave(dto.getEmployeeId(), dto);
        return new ResponseEntity<>(
                ApiResponse.success("Leave request submitted successfully", response),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/employee/{employeeId}")
    @Operation(summary = "Apply for leave (path endpoint)", description = "Allows an employee to submit a leave request with start date, end date, leave type, and reason.")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> applyLeave(
            @PathVariable Long employeeId,
            @Valid @RequestBody LeaveRequestDTO dto) {
        LeaveResponseDTO response = leaveRequestService.applyLeave(employeeId, dto);
        return new ResponseEntity<>(
                ApiResponse.success("Leave request submitted successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(summary = "Get all leave requests", description = "Retrieves all leave requests across the organization, sorted by latest applied date.")
    public ResponseEntity<ApiResponse<List<LeaveResponseDTO>>> getAllLeaves(
            @Parameter(description = "Optional status filter (PENDING, APPROVED, REJECTED)")
            @RequestParam(required = false) LeaveStatus status) {
        List<LeaveResponseDTO> leaves = (status != null) ?
                leaveRequestService.getLeavesByStatus(status) :
                leaveRequestService.getAllLeaves();
        return ResponseEntity.ok(ApiResponse.success("Leave requests retrieved successfully", leaves));
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get leave analytics", description = "Returns aggregated statistics, department distributions, and leave type breakdowns.")
    public ResponseEntity<ApiResponse<LeaveAnalyticsDTO>> getLeaveAnalytics() {
        LeaveAnalyticsDTO analytics = leaveRequestService.getLeaveAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Leave analytics retrieved successfully", analytics));
    }

    @GetMapping("/export")
    @Operation(summary = "Export leaves to CSV", description = "Generates and downloads a CSV file containing all organizational leave records.")
    public ResponseEntity<byte[]> exportLeavesToCsv() {
        String csvData = leaveRequestService.exportLeavesToCsv();
        byte[] output = csvData.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"leave-records.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(output);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get leave request by ID", description = "Retrieves specific leave request details.")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> getLeaveById(@PathVariable Long id) {
        LeaveResponseDTO leave = leaveRequestService.getLeaveById(id);
        return ResponseEntity.ok(ApiResponse.success("Leave request found", leave));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get employee's leave history", description = "Retrieves all leave requests submitted by a specific employee.")
    public ResponseEntity<ApiResponse<List<LeaveResponseDTO>>> getLeavesByEmployee(@PathVariable Long employeeId) {
        List<LeaveResponseDTO> leaves = leaveRequestService.getLeavesByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee leaves retrieved successfully", leaves));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending leaves", description = "Retrieves all leave requests currently waiting for admin review/approval.")
    public ResponseEntity<ApiResponse<List<LeaveResponseDTO>>> getPendingLeaves() {
        List<LeaveResponseDTO> pending = leaveRequestService.getPendingLeaves();
        return ResponseEntity.ok(ApiResponse.success("Pending leaves retrieved successfully", pending));
    }

    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @Operation(summary = "Update leave status (Approve / Reject)", description = "Admin endpoint to approve or reject a leave request with optional remarks.")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> updateLeaveStatus(
            @PathVariable Long id,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) String remarks,
            @RequestBody(required = false) LeaveStatusUpdateDTO body) {
        LeaveStatus finalStatus = (status != null) ? status : (body != null ? body.getStatus() : null);
        if (finalStatus == null) {
            throw new IllegalArgumentException("Status parameter or request body is required (APPROVED, REJECTED, PENDING)");
        }
        String finalRemarks = (remarks != null) ? remarks : (body != null ? body.getManagerRemarks() : null);
        LeaveResponseDTO updated = leaveRequestService.updateLeaveStatus(id, finalStatus, finalRemarks);
        return ResponseEntity.ok(ApiResponse.success("Leave status updated to " + finalStatus, updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a leave request", description = "Deletes a leave request by its ID.")
    public ResponseEntity<ApiResponse<Void>> deleteLeave(@PathVariable Long id) {
        leaveRequestService.deleteLeave(id);
        return ResponseEntity.ok(ApiResponse.success("Leave request deleted successfully", null));
    }
}
