package com.algoworks.leavemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Department is required")
    @Column(nullable = false)
    private String department;

    @NotBlank(message = "Designation is required")
    @Column(nullable = false)
    private String designation;

    @Column(name = "casual_leave_balance", nullable = false)
    private Integer casualLeaveBalance = 12;

    @Column(name = "sick_leave_balance", nullable = false)
    private Integer sickLeaveBalance = 10;

    @Column(name = "annual_leave_balance", nullable = false)
    private Integer annualLeaveBalance = 15;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One employee -> many leave requests.
    // @JsonIgnore prevents circular reference recursion during JSON serialization.
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<LeaveRequest> leaveRequests = new ArrayList<>();

    public Employee() {
    }

    public Employee(Long id, String name, String email, String department, String designation) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.casualLeaveBalance = 12;
        this.sickLeaveBalance = 10;
        this.annualLeaveBalance = 15;
    }

    public Employee(Long id, String name, String email, String department, String designation, Integer casual, Integer sick, Integer annual) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.casualLeaveBalance = (casual != null) ? casual : 12;
        this.sickLeaveBalance = (sick != null) ? sick : 10;
        this.annualLeaveBalance = (annual != null) ? annual : 15;
    }

    public Employee(Long id, String name, String email, String department, String designation, List<LeaveRequest> leaveRequests) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.casualLeaveBalance = 12;
        this.sickLeaveBalance = 10;
        this.annualLeaveBalance = 15;
        this.leaveRequests = (leaveRequests != null) ? leaveRequests : new ArrayList<>();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.casualLeaveBalance == null) this.casualLeaveBalance = 12;
        if (this.sickLeaveBalance == null) this.sickLeaveBalance = 10;
        if (this.annualLeaveBalance == null) this.annualLeaveBalance = 15;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<LeaveRequest> getLeaveRequests() {
        return leaveRequests;
    }

    public void setLeaveRequests(List<LeaveRequest> leaveRequests) {
        this.leaveRequests = leaveRequests;
    }
}
