package com.algoworks.leavemanagement.config;

import com.algoworks.leavemanagement.entity.Employee;
import com.algoworks.leavemanagement.entity.LeaveRequest;
import com.algoworks.leavemanagement.entity.LeaveStatus;
import com.algoworks.leavemanagement.entity.LeaveType;
import com.algoworks.leavemanagement.entity.Notification;
import com.algoworks.leavemanagement.repository.EmployeeRepository;
import com.algoworks.leavemanagement.repository.LeaveRequestRepository;
import com.algoworks.leavemanagement.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Automatically loads realistic sample data if database is empty on application startup.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public DataSeeder(EmployeeRepository employeeRepository,
                      LeaveRequestRepository leaveRequestRepository,
                      NotificationRepository notificationRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run(String... args) {
        seedData(false);
    }

    public synchronized void seedData(boolean force) {
        if (!force && employeeRepository.count() > 0) {
            return;
        }

        if (force) {
            leaveRequestRepository.deleteAll();
            notificationRepository.deleteAll();
            employeeRepository.deleteAll();
        }

        System.out.println(">>> Initializing enterprise sample data for Employee Leave Management System...");

        // 1. Create Sample Employees with Leave Balances
        Employee aliva = employeeRepository.save(
                new Employee(null, "Aliva Sharma", "aliva@example.com", "IT Engineering", "Graduate Trainee", 12, 10, 15)
        );

        Employee priya = employeeRepository.save(
                new Employee(null, "Priya Patel", "priya@example.com", "Human Resources", "HR Executive", 10, 8, 12)
        );

        Employee rahul = employeeRepository.save(
                new Employee(null, "Rahul Verma", "rahul@example.com", "Product Design", "Senior UI/UX Designer", 12, 10, 14)
        );

        Employee sneha = employeeRepository.save(
                new Employee(null, "Sneha Kulkarni", "sneha@example.com", "Quality Assurance", "QA Lead", 11, 10, 15)
        );

        // 2. Create Sample Leave Requests with Leave Types and Remarks
        LocalDate now = LocalDate.now();

        // Aliva: 1 Pending Casual leave
        LeaveRequest leave1 = new LeaveRequest();
        leave1.setEmployee(aliva);
        leave1.setStartDate(now.plusDays(5));
        leave1.setEndDate(now.plusDays(7));
        leave1.setLeaveType(LeaveType.CASUAL);
        leave1.setReason("Personal work & family visit");
        leave1.setStatus(LeaveStatus.PENDING);
        leaveRequestRepository.save(leave1);

        // Priya: 1 Approved Annual leave
        LeaveRequest leave2 = new LeaveRequest();
        leave2.setEmployee(priya);
        leave2.setStartDate(now.plusDays(10));
        leave2.setEndDate(now.plusDays(12));
        leave2.setLeaveType(LeaveType.ANNUAL);
        leave2.setReason("Attending cousin's wedding ceremony");
        leave2.setStatus(LeaveStatus.APPROVED);
        leave2.setManagerRemarks("Approved. Have a wonderful celebration!");
        leaveRequestRepository.save(leave2);

        // Rahul: 1 Rejected Casual leave
        LeaveRequest leave3 = new LeaveRequest();
        leave3.setEmployee(rahul);
        leave3.setStartDate(now.plusDays(2));
        leave3.setEndDate(now.plusDays(3));
        leave3.setLeaveType(LeaveType.CASUAL);
        leave3.setReason("Attending tech symposium during sprint freeze");
        leave3.setStatus(LeaveStatus.REJECTED);
        leave3.setManagerRemarks("Sprint freeze active until Friday. Please reschedule next week.");
        leaveRequestRepository.save(leave3);

        // Sneha: 1 Pending Sick leave
        LeaveRequest leave4 = new LeaveRequest();
        leave4.setEmployee(sneha);
        leave4.setStartDate(now.plusDays(15));
        leave4.setEndDate(now.plusDays(18));
        leave4.setLeaveType(LeaveType.SICK);
        leave4.setReason("Annual health checkup and recovery");
        leave4.setStatus(LeaveStatus.PENDING);
        leaveRequestRepository.save(leave4);

        // 3. Seed Sample Notifications (Email Activity Audit Log)
        notificationRepository.save(new Notification(
                null,
                "hr@organization.com",
                "HR Department",
                "New CASUAL Leave Application",
                "Aliva Sharma (IT Engineering) applied for 3 day(s) from " + now.plusDays(5) + " to " + now.plusDays(7) + ".",
                "LEAVE_SUBMITTED"
        ));

        notificationRepository.save(new Notification(
                null,
                "priya@example.com",
                "Priya Patel",
                "Leave Request #2 APPROVED",
                "Your ANNUAL leave request has been APPROVED. Remarks: Approved. Have a wonderful celebration!",
                "LEAVE_APPROVED"
        ));

        notificationRepository.save(new Notification(
                null,
                "rahul@example.com",
                "Rahul Verma",
                "Leave Request #3 REJECTED",
                "Your CASUAL leave request has been REJECTED. Remarks: Sprint freeze active until Friday. Please reschedule next week.",
                "LEAVE_REJECTED"
        ));

        System.out.println(">>> Enterprise sample data loaded successfully: 4 Employees, 4 Leave Requests, 3 Notification Events.");
    }
}
