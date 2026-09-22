package com.algoworks.leavemanagement.dto;

import com.algoworks.leavemanagement.entity.Employee;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response representation of an employee")
public class EmployeeResponseDTO {

    @Schema(description = "Unique employee identifier", example = "1")
    private Long id;

    @Schema(description = "Full name of the employee", example = "Aliva")
    private String name;

    @Schema(description = "Corporate email", example = "aliva@example.com")
    private String email;

    @Schema(description = "Department", example = "IT")
    private String department;

    @Schema(description = "Designation", example = "Graduate Trainee")
    private String designation;

    @Schema(description = "Remaining Casual Leave Balance (days)", example = "12")
    private Integer casualLeaveBalance = 12;

    @Schema(description = "Remaining Sick Leave Balance (days)", example = "10")
    private Integer sickLeaveBalance = 10;

    @Schema(description = "Remaining Annual Leave Balance (days)", example = "15")
    private Integer annualLeaveBalance = 15;

    @Schema(description = "Timestamp when the employee was added")
    private LocalDateTime createdAt;

    @Schema(description = "Total number of leave requests submitted by this employee", example = "3")
    private int totalLeaveRequests;

    public EmployeeResponseDTO() {
    }

    public EmployeeResponseDTO(Long id, String name, String email, String department, String designation,
                               LocalDateTime createdAt, int totalLeaveRequests) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.casualLeaveBalance = 12;
        this.sickLeaveBalance = 10;
        this.annualLeaveBalance = 15;
        this.createdAt = createdAt;
        this.totalLeaveRequests = totalLeaveRequests;
    }

    public EmployeeResponseDTO(Long id, String name, String email, String department, String designation,
                               Integer casualLeaveBalance, Integer sickLeaveBalance, Integer annualLeaveBalance,
                               LocalDateTime createdAt, int totalLeaveRequests) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.casualLeaveBalance = casualLeaveBalance;
        this.sickLeaveBalance = sickLeaveBalance;
        this.annualLeaveBalance = annualLeaveBalance;
        this.createdAt = createdAt;
        this.totalLeaveRequests = totalLeaveRequests;
    }

    public static EmployeeResponseDTO fromEntity(Employee employee) {
        if (employee == null) {
            return null;
        }
        int totalLeaves = (employee.getLeaveRequests() != null) ? employee.getLeaveRequests().size() : 0;
        return new EmployeeResponseDTO(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getCasualLeaveBalance(),
                employee.getSickLeaveBalance(),
                employee.getAnnualLeaveBalance(),
                employee.getCreatedAt(),
                totalLeaves
        );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Integer getCasualLeaveBalance() {
        return casualLeaveBalance;
    }

    public void setCasualLeaveBalance(Integer casualLeaveBalance) {
        this.casualLeaveBalance = casualLeaveBalance;
    }

    public Integer getSickLeaveBalance() {
        return sickLeaveBalance;
    }

    public void setSickLeaveBalance(Integer sickLeaveBalance) {
        this.sickLeaveBalance = sickLeaveBalance;
    }

    public Integer getAnnualLeaveBalance() {
        return annualLeaveBalance;
    }

    public void setAnnualLeaveBalance(Integer annualLeaveBalance) {
        this.annualLeaveBalance = annualLeaveBalance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getTotalLeaveRequests() {
        return totalLeaveRequests;
    }

    public void setTotalLeaveRequests(int totalLeaveRequests) {
        this.totalLeaveRequests = totalLeaveRequests;
    }
}
