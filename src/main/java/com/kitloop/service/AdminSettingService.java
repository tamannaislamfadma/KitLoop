package com.kitloop.service;

import com.kitloop.entity.AdminSetting;
import com.kitloop.entity.AppUser;
import com.kitloop.entity.Role;
import com.kitloop.repository.AdminSettingRepository;
import com.kitloop.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminSettingService {

    private static final Long SETTINGS_ID = 1L;

    private final AdminSettingRepository
            adminSettingRepository;

    private final AppUserRepository
            appUserRepository;

    public AdminSettingService(
            AdminSettingRepository adminSettingRepository,
            AppUserRepository appUserRepository
    ) {
        this.adminSettingRepository =
                adminSettingRepository;

        this.appUserRepository =
                appUserRepository;
    }

    @Transactional
    public AdminSetting getCurrentSetting() {

        return adminSettingRepository
                .findById(
                        SETTINGS_ID
                )
                .orElseGet(
                        this::createDefaultSetting
                );
    }

    @Transactional
    public AdminSetting updateBkashNumber(
            String bkashNumber,
            AppUser admin
    ) {

        if (admin.getRole()
                != Role.ADMIN) {

            throw new IllegalStateException(
                    "Administrator permission is required."
            );
        }

        AdminSetting setting =
                getCurrentSetting();

        setting.setBkashNumber(
                bkashNumber.trim()
        );

        setting.setUpdatedBy(
                admin
        );

        return adminSettingRepository
                .save(setting);
    }

    private AdminSetting createDefaultSetting() {

        AppUser admin =
                appUserRepository
                        .findFirstByRole(
                                Role.ADMIN
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "KitLoop admin account is not available."
                                )
                        );

        AdminSetting setting =
                new AdminSetting();

        setting.setId(
                SETTINGS_ID
        );

        setting.setBkashNumber(
                admin.getPhone()
        );

        setting.setUpdatedBy(
                admin
        );

        return adminSettingRepository
                .save(setting);
    }
}