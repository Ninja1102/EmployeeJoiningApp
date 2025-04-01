package com.ust.employee_joining.model;

import jakarta.persistence.*;

@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String employeeId;
    private String name;
    private String personalEmail;  // Employee's personal email
    private String companyEmail;   // Generated company email
    private String password;
    private String education;
    private String college;
    private String fathersName;
    private String address;
    private String contact;
    private String panImagePath;
    private String aadharImagePath;
    private String bankPassbookImagePath;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Getters and Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPersonalEmail() { return personalEmail; }
    public void setPersonalEmail(String personalEmail) { this.personalEmail = personalEmail; }

    public String getCompanyEmail() { return companyEmail; }
    public void setCompanyEmail(String companyEmail) { this.companyEmail = companyEmail; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getFathersName() { return fathersName; }
    public void setFathersName(String fathersName) { this.fathersName = fathersName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPanImagePath() {
        return panImagePath;
    }

    public void setPanImagePath(String panImagePath) {
        this.panImagePath = panImagePath;
    }

    public String getAadharImagePath() {
        return aadharImagePath;
    }

    public void setAadharImagePath(String aadharImagePath) {
        this.aadharImagePath = aadharImagePath;
    }

    public String getBankPassbookImagePath() {
        return bankPassbookImagePath;
    }

    public void setBankPassbookImagePath(String bankPassbookImagePath) {
        this.bankPassbookImagePath = bankPassbookImagePath;
    }
}
