package com.kitloop.config;

import com.kitloop.entity.AppUser;
import com.kitloop.entity.Role;
import com.kitloop.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
            );

    private static final Pattern PHONE_PATTERN =
            Pattern.compile(
                    "^01[3-9][0-9]{8}$"
            );

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminPhone;

    public AdminSeeder(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${kitloop.admin.name}")
            String adminName,
            @Value("${kitloop.admin.email}")
            String adminEmail,
            @Value("${kitloop.admin.password}")
            String adminPassword,
            @Value("${kitloop.admin.phone}")
            String adminPhone
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminPhone = adminPhone;
    }

    @Override
    public void run(
            ApplicationArguments args
    ) {

        long adminCount =
                appUserRepository.countByRole(Role.ADMIN);

        if (adminCount > 1) {
            throw new IllegalStateException(
                    "KitLoop security violation: "
                            + "more than one ADMIN account exists."
            );
        }

        if (adminCount == 1) {
            return;
        }

        validateAdminEnvironment();

        String normalizedEmail =
                adminEmail
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (appUserRepository
                .existsByEmailIgnoreCase(normalizedEmail)) {

            throw new IllegalStateException(
                    "Cannot seed KitLoop admin because "
                            + "the configured admin email already "
                            + "belongs to another account."
            );
        }

        AppUser admin = new AppUser();

        admin.setFullName(adminName.trim());
        admin.setEmail(normalizedEmail);
        admin.setPhone(adminPhone.trim());

        admin.setPasswordHash(
                passwordEncoder.encode(adminPassword)
        );

        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);

        appUserRepository.save(admin);
    }

    private void validateAdminEnvironment() {

        if (!StringUtils.hasText(adminName)) {
            throw new IllegalStateException(
                    "KITLOOP_ADMIN_NAME is required."
            );
        }

        if (!StringUtils.hasText(adminEmail)
                || !EMAIL_PATTERN
                .matcher(adminEmail.trim())
                .matches()) {

            throw new IllegalStateException(
                    "KITLOOP_ADMIN_EMAIL must contain "
                            + "a valid email address."
            );
        }

        if (!StringUtils.hasText(adminPhone)
                || !PHONE_PATTERN
                .matcher(adminPhone.trim())
                .matches()) {

            throw new IllegalStateException(
                    "KITLOOP_ADMIN_PHONE must contain "
                            + "a valid Bangladesh mobile number."
            );
        }

        if (!StringUtils.hasText(adminPassword)
                || adminPassword.length() < 8) {

            throw new IllegalStateException(
                    "KITLOOP_ADMIN_PASSWORD must contain "
                            + "at least 8 characters."
            );
        }
    }
}