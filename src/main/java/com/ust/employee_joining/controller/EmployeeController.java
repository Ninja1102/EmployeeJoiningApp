package com.ust.employee_joining.controller;

import com.ust.employee_joining.model.Employee;
import com.ust.employee_joining.model.Role;
import com.ust.employee_joining.repository.EmployeeRepository;
import com.ust.employee_joining.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeService employeeService, EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Employee> registerEmployee(@RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.registerEmployee(employee));
    }

    @GetMapping
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Optional<Employee>> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee updatedEmployee) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, updatedEmployee));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee deleted successfully.");
    }

    @PostMapping("/{id}/upload/{type}")
    public ResponseEntity<String> uploadDocument(
            @PathVariable Long id,
            @PathVariable String type,
            @RequestParam("file") MultipartFile file) throws IOException {

        // Get current user's email from JWT
        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        // Fetch employee details
        Employee currentEmployee = employeeRepository.findByCompanyEmail(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Authorization check
        if (!currentEmployee.getId().equals(id) &&
                !currentEmployee.getRole().equals(Role.HR) &&
                !currentEmployee.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("You can only upload your own documents");
        }

        return ResponseEntity.ok(employeeService.uploadDocument(id, file, type));
    }

}

