package com.kitloop.service;

import com.kitloop.exception.AuthenticationFlowException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpMailService {

    private final JavaMailSender javaMailSender;
    private final String fromAddress;

    public OtpMailService(
            JavaMailSender javaMailSender,
            @Value("${kitloop.mail.from}") String fromAddress
    ) {
        this.javaMailSender = javaMailSender;
        this.fromAddress = fromAddress;
    }

    public void sendRegistrationOtp(
            String recipientEmail,
            String fullName,
            String otp,
            int expiryMinutes
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject("KitLoop Account Verification");

        message.setText(
                "Hello " + fullName + ",\n\n"
                        + "Your KitLoop verification code is:\n\n"
                        + otp
                        + "\n\nThis code expires in "
                        + expiryMinutes
                        + " minutes.\n\n"
                        + "If you did not request a KitLoop account, "
                        + "you can ignore this email.\n\n"
                        + "KitLoop\n"
                        + "Share. Rent. Reuse."
        );

        try {
            javaMailSender.send(message);
        } catch (MailException exception) {
            throw new AuthenticationFlowException(
                    "KitLoop could not send the verification email. "
                            + "Please check the mail configuration and try again."
            );
        }
    }
}