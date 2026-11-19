package com.kitloop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationRequest {

    @NotBlank(message = "Full name is required.")
    @Size(
            min = 2,
            max = 100,
            message = "Full name must be between 2 and 100 characters."
    )
    private String fullName;

    @NotBlank(message = "DIU email is required.")
    @Email(message = "Enter a valid email address.")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@diu\\.edu\\.bd$",
            message = "Only @diu.edu.bd email addresses are allowed."
    )
    private String email;

    @NotBlank(message = "Phone/bKash number is required.")
    @Pattern(
            regexp = "^01[3-9][0-9]{8}$",
            message = "Enter a valid 11-digit Bangladesh mobile number."
    )
    private String phone;

    @NotBlank(message = "Password is required.")
    @Size(
            min = 8,
            max = 72,
            message = "Password must be between 8 and 72 characters."
    )
    private String password;

    @NotBlank(message = "Confirm password is required.")
    private String confirmPassword;

    public RegistrationRequest() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}