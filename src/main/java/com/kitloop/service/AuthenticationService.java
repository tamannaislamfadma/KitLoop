package com.kitloop.service;

import com.kitloop.dto.RegistrationRequest;
import com.kitloop.entity.AppUser;
import com.kitloop.entity.PendingRegistration;
import com.kitloop.entity.Role;
import com.kitloop.exception.AuthenticationFlowException;
import com.kitloop.repository.AppUserRepository;
import com.kitloop.repository.PendingRegistrationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AuthenticationService {

    private static final Pattern DIU_EMAIL_PATTERN =
            Pattern.compile(
                    "^[a-z0-9._%+-]+@diu\\.edu\\.bd$"
            );

    private static final int MAX_OTP_ATTEMPTS = 5;

    private final AppUserRepository appUserRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpMailService otpMailService;
    private final SecureRandom secureRandom;

    private final int otpExpiryMinutes;
    private final long resendCooldownSeconds;

    public AuthenticationService(
            AppUserRepository appUserRepository,
            PendingRegistrationRepository pendingRegistrationRepository,
            PasswordEncoder passwordEncoder,
            OtpMailService otpMailService,
            @Value("${kitloop.otp.expiry-minutes:5}")
            int otpExpiryMinutes,
            @Value("${kitloop.otp.resend-cooldown-seconds:60}")
            long resendCooldownSeconds
    ) {
        this.appUserRepository = appUserRepository;
        this.pendingRegistrationRepository =
                pendingRegistrationRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpMailService = otpMailService;
        this.secureRandom = new SecureRandom();
        this.otpExpiryMinutes = otpExpiryMinutes;
        this.resendCooldownSeconds = resendCooldownSeconds;
    }

    @Transactional
    public void startRegistration(
            RegistrationRequest request
    ) {

        String email = normalizeEmail(request.getEmail());
        String fullName = request.getFullName().trim();
        String phone = request.getPhone().trim();

        validateDiuEmail(email);

        if (!request.getPassword().equals(
                request.getConfirmPassword()
        )) {
            throw new AuthenticationFlowException(
                    "Password and confirm password do not match."
            );
        }

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new AuthenticationFlowException(
                    "An account already exists with this email."
            );
        }

        PendingRegistration pendingRegistration =
                pendingRegistrationRepository
                        .findByEmailIgnoreCase(email)
                        .orElseGet(PendingRegistration::new);

        String otp = generateOtp();
        Instant now = Instant.now();

        pendingRegistration.setFullName(fullName);
        pendingRegistration.setEmail(email);
        pendingRegistration.setPhone(phone);

        pendingRegistration.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        pendingRegistration.setOtpHash(
                passwordEncoder.encode(otp)
        );

        pendingRegistration.setOtpSentAt(now);

        pendingRegistration.setOtpExpiresAt(
                now.plus(
                        Duration.ofMinutes(
                                otpExpiryMinutes
                        )
                )
        );

        pendingRegistration.setFailedAttempts(0);

        pendingRegistrationRepository.save(
                pendingRegistration
        );

        otpMailService.sendRegistrationOtp(
                email,
                fullName,
                otp,
                otpExpiryMinutes
        );
    }

    @Transactional(
            noRollbackFor = AuthenticationFlowException.class
    )
    public void verifyOtp(
            String emailInput,
            String otp
    ) {

        String email = normalizeEmail(emailInput);

        validateDiuEmail(email);

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            pendingRegistrationRepository
                    .findByEmailIgnoreCase(email)
                    .ifPresent(
                            pendingRegistrationRepository::delete
                    );

            throw new AuthenticationFlowException(
                    "This account has already been verified. "
                            + "Please sign in."
            );
        }

        PendingRegistration pendingRegistration =
                pendingRegistrationRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new AuthenticationFlowException(
                                        "No pending registration was found. "
                                                + "Please create your account again."
                                )
                        );

        if (pendingRegistration.getFailedAttempts()
                >= MAX_OTP_ATTEMPTS) {

            throw new AuthenticationFlowException(
                    "Too many incorrect verification attempts. "
                            + "Please request a new code."
            );
        }

        Instant now = Instant.now();

        if (now.isAfter(
                pendingRegistration.getOtpExpiresAt()
        )) {
            throw new AuthenticationFlowException(
                    "Your verification code has expired. "
                            + "Please request a new code."
            );
        }

        boolean matches = passwordEncoder.matches(
                otp,
                pendingRegistration.getOtpHash()
        );

        if (!matches) {

            int newAttemptCount =
                    pendingRegistration.getFailedAttempts() + 1;

            pendingRegistration.setFailedAttempts(
                    newAttemptCount
            );

            pendingRegistrationRepository.save(
                    pendingRegistration
            );

            if (newAttemptCount >= MAX_OTP_ATTEMPTS) {
                throw new AuthenticationFlowException(
                        "Too many incorrect verification attempts. "
                                + "Please request a new code."
                );
            }

            int attemptsRemaining =
                    MAX_OTP_ATTEMPTS - newAttemptCount;

            throw new AuthenticationFlowException(
                    "Incorrect verification code. "
                            + attemptsRemaining
                            + " attempt(s) remaining."
            );
        }

        AppUser user = new AppUser();

        user.setFullName(
                pendingRegistration.getFullName()
        );

        user.setEmail(
                pendingRegistration.getEmail()
        );

        user.setPhone(
                pendingRegistration.getPhone()
        );

        user.setPasswordHash(
                pendingRegistration.getPasswordHash()
        );

        user.setRole(Role.USER);
        user.setEnabled(true);

        appUserRepository.saveAndFlush(user);

        pendingRegistrationRepository.delete(
                pendingRegistration
        );
    }

    @Transactional
    public void resendOtp(String emailInput) {

        String email = normalizeEmail(emailInput);

        validateDiuEmail(email);

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new AuthenticationFlowException(
                    "This account has already been verified. "
                            + "Please sign in."
            );
        }

        PendingRegistration pendingRegistration =
                pendingRegistrationRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new AuthenticationFlowException(
                                        "No pending registration was found. "
                                                + "Please create your account again."
                                )
                        );

        Instant now = Instant.now();

        if (pendingRegistration.getFailedAttempts()
                < MAX_OTP_ATTEMPTS) {

            Instant resendAllowedAt =
                    pendingRegistration
                            .getOtpSentAt()
                            .plusSeconds(
                                    resendCooldownSeconds
                            );

            if (now.isBefore(resendAllowedAt)) {

                long secondsRemaining =
                        Duration.between(
                                now,
                                resendAllowedAt
                        ).toSeconds() + 1;

                throw new AuthenticationFlowException(
                        "Please wait "
                                + secondsRemaining
                                + " second(s) before requesting another code."
                );
            }
        }

        String newOtp = generateOtp();

        pendingRegistration.setOtpHash(
                passwordEncoder.encode(newOtp)
        );

        pendingRegistration.setOtpSentAt(now);

        pendingRegistration.setOtpExpiresAt(
                now.plus(
                        Duration.ofMinutes(
                                otpExpiryMinutes
                        )
                )
        );

        pendingRegistration.setFailedAttempts(0);

        pendingRegistrationRepository.save(
                pendingRegistration
        );

        otpMailService.sendRegistrationOtp(
                pendingRegistration.getEmail(),
                pendingRegistration.getFullName(),
                newOtp,
                otpExpiryMinutes
        );
    }

    @Transactional(readOnly = true)
    public boolean hasPendingRegistration(
            String emailInput
    ) {

        if (emailInput == null) {
            return false;
        }

        String email = normalizeEmail(emailInput);

        return pendingRegistrationRepository
                .existsByEmailIgnoreCase(email);
    }

    public int getOtpExpiryMinutes() {
        return otpExpiryMinutes;
    }

    private String generateOtp() {

        int number = secureRandom.nextInt(1_000_000);

        return String.format(
                Locale.ROOT,
                "%06d",
                number
        );
    }

    private String normalizeEmail(String email) {

        if (email == null) {
            return "";
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private void validateDiuEmail(String email) {

        if (!DIU_EMAIL_PATTERN
                .matcher(email)
                .matches()) {

            throw new AuthenticationFlowException(
                    "Only @diu.edu.bd email addresses are allowed."
            );
        }
    }
}