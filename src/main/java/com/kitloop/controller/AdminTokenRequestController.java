package com.kitloop.controller;

import com.kitloop.entity.AppUser;
import com.kitloop.entity.EquipmentRequest;
import com.kitloop.entity.Role;
import com.kitloop.exception.RequestFlowException;
import com.kitloop.repository.AppUserRepository;
import com.kitloop.service.EquipmentRequestService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/token-payments")
public class AdminTokenRequestController {

    private final AppUserRepository
            appUserRepository;

    private final EquipmentRequestService
            equipmentRequestService;

    public AdminTokenRequestController(
            AppUserRepository appUserRepository,
            EquipmentRequestService equipmentRequestService
    ) {
        this.appUserRepository =
                appUserRepository;

        this.equipmentRequestService =
                equipmentRequestService;
    }

    @GetMapping
    public String tokenPayments(
            Authentication authentication,
            Model model
    ) {

        AppUser admin =
                loadAdmin(
                        authentication
                );

        List<EquipmentRequest> requests =
                equipmentRequestService
                        .getFreeShareRequests();

        model.addAttribute(
                "currentUser",
                admin
        );

        model.addAttribute(
                "activePage",
                "payments"
        );

        model.addAttribute(
                "pageTitle",
                "Token Payments"
        );

        model.addAttribute(
                "pageSubtitle",
                "Review FREE_SHARE payment proofs."
        );

        model.addAttribute(
                "requests",
                requests
        );

        return "admin/token-payments";
    }

    @PostMapping("/{requestId}/approve")
    public String approve(
            @PathVariable
            Long requestId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        AppUser admin =
                loadAdmin(
                        authentication
                );

        try {

            equipmentRequestService
                    .approveByAdmin(
                            requestId,
                            admin
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "decisionSuccess",
                            "FREE_SHARE payment approved. Wallet and trust score were updated exactly once."
                    );

        } catch (
                RequestFlowException exception
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "decisionError",
                            exception.getMessage()
                    );
        }

        return "redirect:/admin/token-payments";
    }

    @PostMapping("/{requestId}/reject")
    public String reject(
            @PathVariable
            Long requestId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        AppUser admin =
                loadAdmin(
                        authentication
                );

        try {

            equipmentRequestService
                    .rejectByAdmin(
                            requestId,
                            admin
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "decisionSuccess",
                            "FREE_SHARE payment rejected."
                    );

        } catch (
                RequestFlowException exception
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "decisionError",
                            exception.getMessage()
                    );
        }

        return "redirect:/admin/token-payments";
    }

    private AppUser loadAdmin(
            Authentication authentication
    ) {

        AppUser admin =
                appUserRepository
                        .findByEmailIgnoreCase(
                                authentication.getName()
                        )
                        .orElseThrow();

        if (admin.getRole()
                != Role.ADMIN) {

            throw new IllegalStateException(
                    "Administrator permission is required."
            );
        }

        return admin;
    }
}