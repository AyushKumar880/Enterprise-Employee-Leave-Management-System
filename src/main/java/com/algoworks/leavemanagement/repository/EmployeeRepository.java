package com.algoworks.leavemanagement.repository;

import com.algoworks.leavemanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Employee> findByDepartmentIgnoreCase(String department);

    List<Employee> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}
