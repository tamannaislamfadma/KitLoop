package com.kitloop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class EquipmentRequestForm {

    @NotBlank(
            message = "Transaction ID is required."
    )
    @Size(
            min = 6,
            max = 40,
            message = "Transaction ID must be between 6 and 40 characters."
    )
    @Pattern(
            regexp = "^[A-Za-z0-9]+$",
            message = "Transaction ID may contain only letters and numbers."
    )
    private String transactionId;

    @NotBlank(
            message = "Sender bKash number is required."
    )
    @Pattern(
            regexp = "^01[3-9][0-9]{8}$",
            message = "Enter a valid 11-digit Bangladesh mobile number."
    )
    private String senderNumber;

    private MultipartFile paymentScreenshot;

    public EquipmentRequestForm() {
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(
            String transactionId
    ) {
        this.transactionId = transactionId;
    }

    public String getSenderNumber() {
        return senderNumber;
    }

    public void setSenderNumber(
            String senderNumber
    ) {
        this.senderNumber = senderNumber;
    }

    public MultipartFile getPaymentScreenshot() {
        return paymentScreenshot;
    }

    public void setPaymentScreenshot(
            MultipartFile paymentScreenshot
    ) {
        this.paymentScreenshot =
                paymentScreenshot;
    }
}