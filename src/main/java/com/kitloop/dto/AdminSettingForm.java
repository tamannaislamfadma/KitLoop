package com.kitloop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AdminSettingForm {

    @NotBlank(
            message = "Admin bKash number is required."
    )
    @Pattern(
            regexp = "^01[3-9][0-9]{8}$",
            message = "Enter a valid 11-digit Bangladesh mobile number."
    )
    private String bkashNumber;

    public AdminSettingForm() {
    }

    public String getBkashNumber() {
        return bkashNumber;
    }

    public void setBkashNumber(
            String bkashNumber
    ) {
        this.bkashNumber = bkashNumber;
    }
}