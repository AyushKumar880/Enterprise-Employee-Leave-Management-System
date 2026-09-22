package com.algoworks.leavemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating or updating an employee")
public class EmployeeRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Full name of the employee", example = "Aliva")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Schema(description = "Corporate email address of the employee", example = "aliva@example.com")
    private String email;

    @NotBlank(message = "Department is required")
    @Schema(description = "Department name", example = "IT")
    private String department;

    @NotBlank(message = "Designation is required")
    @Schema(description = "Job designation / title", example = "Graduate Trainee")
    private String designation;

    public EmployeeRequestDTO() {
    }

    public EmployeeRequestDTO(String name, String email, String department, String designation) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
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
}
