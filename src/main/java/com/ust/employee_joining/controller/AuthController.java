package com.ust.employee_joining.controller;

import com.ust.employee_joining.model.Role;
import com.ust.employee_joining.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        Role role = Role.valueOf(request.get("role").toUpperCase());

        return ResponseEntity.ok(authService.register(username, email, password, role));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        return ResponseEntity.ok(authService.login(username, password));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {

        return ResponseEntity.ok("Logged out successfully!");
    }

    @DeleteMapping("/delete-admin/{username}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")  // Restrict to ADMIN & HR
    public ResponseEntity<String> deleteAdmin(@PathVariable String username) {
        authService.deleteAdmin(username);
        return ResponseEntity.ok("Admin deleted successfully.");
    }
}