package com.kitloop.controller;

import com.kitloop.dto.AdminDashboardStats;
import com.kitloop.dto.AdminSettingForm;
import com.kitloop.entity.AdminSetting;
import com.kitloop.entity.AppUser;
import com.kitloop.entity.Role;
import com.kitloop.entity.UserNotification;
import com.kitloop.repository.AppUserRepository;
import com.kitloop.service.AdminDashboardService;
import com.kitloop.service.AdminSettingService;
import com.kitloop.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminPortalController {

    private final AppUserRepository
            appUserRepository;

    private final AdminDashboardService
            adminDashboardService;

    private final AdminSettingService
            adminSettingService;

    private final NotificationService
            notificationService;

    public AdminPortalController(
            AppUserRepository appUserRepository,
            AdminDashboardService adminDashboardService,
            AdminSettingService adminSettingService,
            NotificationService notificationService
    ) {
        this.appUserRepository =
                appUserRepository;

        this.adminDashboardService =
                adminDashboardService;

        this.adminSettingService =
                adminSettingService;

        this.notificationService =
                notificationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model
    ) {

        AppUser admin =
                loadAdmin(
                        authentication
                );

        AdminDashboardStats stats =
                adminDashboardService
                        .getStats();

        prepareCommon(
                model,
                admin,
                "dashboard",
                "Admin Dashboard",
                "Monitor KitLoop marketplace operations."
        );

        model.addAttribute(
                "stats",
                stats
        );

        return "admin/dashboard";
    }

    @GetMapping("/settings")
    public String settings(
            Authentication authentication,
            Model model
    ) {

        AppUser admin =
                loadAdmin(
                        authentication
                );

        AdminSetting setting =
                adminSettingService
                        .getCurrentSetting();

        AdminSettingForm form =
                new AdminSettingForm();

        form.setBkashNumber(
                setting.getBkashNumber()
        );

        prepareCommon(
                model,
                admin,
                "settings",
                "Admin Settings",
                "Manage the bKash number used for FREE_SHARE payments."
        );

        model.addAttribute(
                "settingForm",
                form
        );

        return "admin/settings";
    }

    @PostMapping("/settings")
    public String updateSettings(
            Authentication authentication,
            @Valid
            @ModelAttribute("settingForm")
            AdminSettingForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        AppUser admin =
                loadAdmin(
                        authentication
                );

        if (bindingResult.hasErrors()) {

            prepareCommon(
                    model,
                    admin,
                    "settings",
                    "Admin Settings",
                    "Manage the bKash number used for FREE_SHARE payments."
            );

            return "admin/settings";
        }

        adminSettingService.updateBkashNumber(
                form.getBkashNumber(),
                admin
        );

        redirectAttributes.addFlashAttribute(
                "settingsSuccess",
                "Admin bKash number updated successfully."
        );

        return "redirect:/admin/settings";
    }

    @GetMapping("/notifications")
    public String notifications(
            Authentication authentication,
            Model model
    ) {

        AppUser admin =
                loadAdmin(
                        authentication
                );

        List<UserNotification> notifications =
                notificationService
                        .getNotifications(
                                admin.getId()
                        );

        notificationService.markAllRead(
                admin.getId()
        );

        prepareCommon(
                model,
                admin,
                "notifications",
                "Notifications",
                "Review KitLoop payment and request activity."
        );

        model.addAttribute(
                "notifications",
                notifications
        );

        return "admin/notifications";
    }

    private void prepareCommon(
            Model model,
            AppUser admin,
            String activePage,
            String pageTitle,
            String pageSubtitle
    ) {

        model.addAttribute(
                "currentUser",
                admin
        );

        model.addAttribute(
                "activePage",
                activePage
        );

        model.addAttribute(
                "pageTitle",
                pageTitle
        );

        model.addAttribute(
                "pageSubtitle",
                pageSubtitle
        );
    }

    private AppUser loadAdmin(
            Authentication authentication
    ) {

        AppUser admin =
                appUserRepository
                        .findByEmailIgnoreCase(
                                authentication.getName()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Authenticated admin no longer exists."
                                )
                        );

        if (admin.getRole()
                != Role.ADMIN) {

            throw new IllegalStateException(
                    "Administrator permission is required."
            );
        }

        return admin;
    }
}