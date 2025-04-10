package com.ust.employee_joining.service;

import com.ust.employee_joining.model.Employee;
import com.ust.employee_joining.model.Role;
import com.ust.employee_joining.model.User;
import com.ust.employee_joining.repository.EmployeeRepository;
import com.ust.employee_joining.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeeService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.max-size}")
    private String maxFileSize;

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
        // Validate file size
        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new RuntimeException("File size exceeds 5MB limit");
        }

        // Create secure filename
        String fileName = String.format("%s-%d-%s",
                type,
                employeeId,
                UUID.randomUUID().toString().substring(0, 8)
        );

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update entity
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        switch (type.toLowerCase()) {
            case "pan" -> employee.setPanImagePath(filePath.toString());
            case "aadhar" -> employee.setAadharImagePath(filePath.toString());
            case "passbook" -> employee.setBankPassbookImagePath(filePath.toString());
            default -> throw new IllegalArgumentException("Invalid document type");
        }

        employeeRepository.save(employee);
        return "File uploaded successfully";
    }
}
