package com.kitloop.dto;

import java.math.BigDecimal;

public class PaymentInstructions {

    private final BigDecimal amount;
    private final String receiverName;
    private final String receiverNumber;
    private final String receiverLabel;

    public PaymentInstructions(
            BigDecimal amount,
            String receiverName,
            String receiverNumber,
            String receiverLabel
    ) {
        this.amount = amount;
        this.receiverName = receiverName;
        this.receiverNumber = receiverNumber;
        this.receiverLabel = receiverLabel;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverNumber() {
        return receiverNumber;
    }

    public String getReceiverLabel() {
        return receiverLabel;
    }
}