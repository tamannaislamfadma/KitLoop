package com.kitloop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OtpVerificationRequest {

    @NotBlank(message = "Email is required.")
    private String email;

    @NotBlank(message = "Verification code is required.")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "Verification code must contain exactly 6 digits."
    )
    private String otp;

    public OtpVerificationRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}