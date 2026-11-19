package com.kitloop.controller;

import com.kitloop.dto.OtpVerificationRequest;
import com.kitloop.dto.RegistrationRequest;
import com.kitloop.exception.AuthenticationFlowException;
import com.kitloop.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Locale;

@Controller
public class RegistrationController {

    private final AuthenticationService authenticationService;

    public RegistrationController(
            AuthenticationService authenticationService
    ) {
        this.authenticationService =
                authenticationService;
    }

    @PostMapping("/register")
    public String register(
            @Valid
            @ModelAttribute("registrationRequest")
            RegistrationRequest registrationRequest,
            BindingResult bindingResult,
            Model model
    ) {

        if (registrationRequest.getPassword() != null
                && registrationRequest
                .getConfirmPassword() != null
                && !registrationRequest
                .getPassword()
                .equals(
                        registrationRequest
                                .getConfirmPassword()
                )) {

            bindingResult.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    "Password and confirm password do not match."
            );
        }

        if (bindingResult.hasErrors()) {

            prepareRegistrationPage(model);

            return "index";
        }

        try {

            authenticationService.startRegistration(
                    registrationRequest
            );

        } catch (
                AuthenticationFlowException exception
        ) {

            model.addAttribute(
                    "registrationError",
                    exception.getMessage()
            );

            prepareRegistrationPage(model);

            return "index";
        }

        return "redirect:"
                + buildOtpUrl(
                registrationRequest.getEmail()
        );
    }

    @GetMapping("/verify-otp")
    public String otpPage(
            @RequestParam("email")
            String email,
            @RequestParam(
                    name = "resent",
                    required = false
            )
            String resent,
            Model model
    ) {

        String normalizedEmail =
                normalizeEmail(email);

        if (!authenticationService
                .hasPendingRegistration(
                        normalizedEmail
                )) {

            return "redirect:/?registrationExpired";
        }

        OtpVerificationRequest request =
                new OtpVerificationRequest();

        request.setEmail(normalizedEmail);

        model.addAttribute(
                "otpVerificationRequest",
                request
        );

        prepareOtpPage(
                model,
                normalizedEmail
        );

        model.addAttribute(
                "resent",
                resent != null
        );

        return "auth/otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @Valid
            @ModelAttribute("otpVerificationRequest")
            OtpVerificationRequest request,
            BindingResult bindingResult,
            Model model
    ) {

        String normalizedEmail =
                normalizeEmail(
                        request.getEmail()
                );

        request.setEmail(normalizedEmail);

        if (bindingResult.hasErrors()) {

            prepareOtpPage(
                    model,
                    normalizedEmail
            );

            return "auth/otp";
        }

        try {

            authenticationService.verifyOtp(
                    normalizedEmail,
                    request.getOtp()
            );

        } catch (
                AuthenticationFlowException exception
        ) {

            model.addAttribute(
                    "otpError",
                    exception.getMessage()
            );

            prepareOtpPage(
                    model,
                    normalizedEmail
            );

            return "auth/otp";
        }

        return "redirect:/?registered";
    }

    @PostMapping("/resend-otp")
    public String resendOtp(
            @RequestParam("email")
            String email,
            Model model
    ) {

        String normalizedEmail =
                normalizeEmail(email);

        try {

            authenticationService.resendOtp(
                    normalizedEmail
            );

        } catch (
                AuthenticationFlowException exception
        ) {

            OtpVerificationRequest request =
                    new OtpVerificationRequest();

            request.setEmail(normalizedEmail);

            model.addAttribute(
                    "otpVerificationRequest",
                    request
            );

            model.addAttribute(
                    "resendError",
                    exception.getMessage()
            );

            prepareOtpPage(
                    model,
                    normalizedEmail
            );

            return "auth/otp";
        }

        return "redirect:"
                + buildOtpUrl(normalizedEmail)
                + "&resent";
    }

    private void prepareRegistrationPage(
            Model model
    ) {

        model.addAttribute(
                "activeTab",
                "register"
        );

        model.addAttribute(
                "loginError",
                false
        );

        model.addAttribute(
                "logoutSuccess",
                false
        );

        model.addAttribute(
                "registrationSuccess",
                false
        );

        model.addAttribute(
                "registrationExpired",
                false
        );
    }

    private void prepareOtpPage(
            Model model,
            String email
    ) {

        model.addAttribute(
                "email",
                email
        );

        model.addAttribute(
                "otpExpiryMinutes",
                authenticationService
                        .getOtpExpiryMinutes()
        );
    }

    private String buildOtpUrl(String email) {

        return UriComponentsBuilder
                .fromPath("/verify-otp")
                .queryParam(
                        "email",
                        normalizeEmail(email)
                )
                .build()
                .encode()
                .toUriString();
    }

    private String normalizeEmail(String email) {

        if (email == null) {
            return "";
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}