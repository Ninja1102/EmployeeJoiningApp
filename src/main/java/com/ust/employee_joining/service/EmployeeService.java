package com.ust.employee_joining.service;

import com.ust.employee_joining.model.Employee;
import com.ust.employee_joining.model.Role;
import com.ust.employee_joining.model.User;
import com.ust.employee_joining.repository.EmployeeRepository;
import com.ust.employee_joining.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    public EmployeeService(EmployeeRepository employeeRepository,
                           UserRepository userRepository,  // ✅ Injected properly
                           PasswordEncoder passwordEncoder,
                           EmailService emailService) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public Employee registerEmployee(Employee employee) {
        // Generate unique employee ID
        String employeeId = "EMP-" + UUID.randomUUID().toString().substring(0, 8);
        employee.setEmployeeId(employeeId);

        // Generate company email based on employee ID
        String companyEmail = employeeId.toLowerCase() + "@company.com";
        employee.setCompanyEmail(companyEmail);

        // Generate a temporary password
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        employee.setPassword(passwordEncoder.encode(tempPassword));

        // Set default role as EMPLOYEE
        employee.setRole(Role.EMPLOYEE);

        // Save employee in the employee table
        Employee savedEmployee = employeeRepository.save(employee);

        // Also, save employee in the users table
        User newUser = new User();
        newUser.setUsername(companyEmail);  // Use company email as username
        newUser.setPassword(employee.getPassword()); // Store encoded password
        newUser.setRole(Role.EMPLOYEE); // Set role as EMPLOYEE

        userRepository.save(newUser); // Save in users table

        // Send temporary password via email
        emailService.sendEmail(
                employee.getPersonalEmail(),
                "Welcome to the Company",
                "Your account has been created. Your company email is: " + companyEmail +
                        "\nYour temporary password is: " + tempPassword +
                        "\nPlease log in and change your password."
        );

        return savedEmployee;
    }


    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employee.setName(updatedEmployee.getName());
                    employee.setContact(updatedEmployee.getContact());
                    employee.setAddress(updatedEmployee.getAddress());
                    return employeeRepository.save(employee);
                }).orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found with ID: " + id);
        }
        employeeRepository.deleteById(id);
    }


    public String uploadDocument(Long employeeId, MultipartFile file, String type) throws IOException {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Create upload directory if it doesn't exist
        File uploadDir = new File("uploads/");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Generate unique filename
        String filePath = "uploads/" + type + "-" + employeeId + "-" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
        File destFile = new File(filePath);
        file.transferTo(destFile);

        // Save file path in DB based on type
        switch (type.toLowerCase()) {
            case "pan" -> employee.setPanImagePath(filePath);
            case "aadhar" -> employee.setAadharImagePath(filePath);
            case "passbook" -> employee.setBankPassbookImagePath(filePath);
            default -> throw new IllegalArgumentException("Invalid document type");
        }

        employeeRepository.save(employee);
        return "File uploaded successfully: " + filePath;
    }
}
