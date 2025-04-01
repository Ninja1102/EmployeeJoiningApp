package com.ust.employee_joining.repository;

import com.ust.employee_joining.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByCompanyEmail(String companyEmail);
}
