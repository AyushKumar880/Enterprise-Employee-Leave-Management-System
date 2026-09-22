package com.algoworks.leavemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Analytics overview of employees and leave requests across the company")
public class LeaveAnalyticsDTO {

    private long totalEmployees;
    private long totalLeaveRequests;
    private long pendingRequests;
    private long approvedRequests;
    private long rejectedRequests;
    private double approvalRate;
    private Map<String, Long> leavesByDepartment;
    private Map<String, Long> leavesByType;

    public LeaveAnalyticsDTO() {
    }

    public LeaveAnalyticsDTO(long totalEmployees, long totalLeaveRequests, long pendingRequests,
                             long approvedRequests, long rejectedRequests, double approvalRate,
                             Map<String, Long> leavesByDepartment, Map<String, Long> leavesByType) {
        this.totalEmployees = totalEmployees;
        this.totalLeaveRequests = totalLeaveRequests;
        this.pendingRequests = pendingRequests;
        this.approvedRequests = approvedRequests;
        this.rejectedRequests = rejectedRequests;
        this.approvalRate = approvalRate;
        this.leavesByDepartment = leavesByDepartment;
        this.leavesByType = leavesByType;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getTotalLeaveRequests() {
        return totalLeaveRequests;
    }

    public void setTotalLeaveRequests(long totalLeaveRequests) {
        this.totalLeaveRequests = totalLeaveRequests;
    }

    public long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public long getApprovedRequests() {
        return approvedRequests;
    }

    public void setApprovedRequests(long approvedRequests) {
        this.approvedRequests = approvedRequests;
    }

    public long getRejectedRequests() {
        return rejectedRequests;
    }

    public void setRejectedRequests(long rejectedRequests) {
        this.rejectedRequests = rejectedRequests;
    }

    public double getApprovalRate() {
        return approvalRate;
    }

    public void setApprovalRate(double approvalRate) {
        this.approvalRate = approvalRate;
    }

    public Map<String, Long> getLeavesByDepartment() {
        return leavesByDepartment;
    }

    public void setLeavesByDepartment(Map<String, Long> leavesByDepartment) {
        this.leavesByDepartment = leavesByDepartment;
    }

    public Map<String, Long> getLeavesByType() {
        return leavesByType;
    }

    public void setLeavesByType(Map<String, Long> leavesByType) {
        this.leavesByType = leavesByType;
    }
}
