package com.kitloop.controller;

import com.kitloop.entity.AppUser;
import com.kitloop.entity.EquipmentRequest;
import com.kitloop.entity.UserNotification;
import com.kitloop.exception.RequestFlowException;
import com.kitloop.repository.AppUserRepository;
import com.kitloop.service.EquipmentRequestService;
import com.kitloop.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class NotificationController {

    private final AppUserRepository appUserRepository;
    private final EquipmentRequestService equipmentRequestService;
    private final NotificationService notificationService;

    public NotificationController(
            AppUserRepository appUserRepository,
            EquipmentRequestService equipmentRequestService,
            NotificationService notificationService
    ) {
        this.appUserRepository = appUserRepository;
        this.equipmentRequestService = equipmentRequestService;
        this.notificationService = notificationService;
    }

    @GetMapping("/user/notifications")
    public String notifications(
            Authentication authentication,
            Model model
    ) {

        AppUser currentUser =
                loadCurrentUser(
                        authentication
                );

        List<UserNotification> notifications =
                notificationService
                        .getNotifications(
                                currentUser.getId()
                        );

        List<EquipmentRequest> incoming =
                equipmentRequestService
                        .getIncomingRequests(
                                currentUser.getId()
                        );

        List<EquipmentRequest> outgoing =
                equipmentRequestService
                        .getOutgoingRequests(
                                currentUser.getId()
                        );

        notificationService.markAllRead(
                currentUser.getId()
        );

        model.addAttribute(
                "currentUser",
                currentUser
        );

        model.addAttribute(
                "activePage",
                "notifications"
        );

        model.addAttribute(
                "pageTitle",
                "Notifications"
        );

        model.addAttribute(
                "pageSubtitle",
                "Review requests, payments and KitLoop updates."
        );

        model.addAttribute(
                "notifications",
                notifications
        );

        model.addAttribute(
                "incomingRequests",
                incoming
        );

        model.addAttribute(
                "outgoingRequests",
                outgoing
        );

        return "user/notifications";
    }

    @PostMapping(
            "/user/requests/{requestId}/approve"
    )
    public String approve(
            @PathVariable Long requestId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        AppUser currentUser =
                loadCurrentUser(
                        authentication
                );

        try {

            equipmentRequestService
                    .approveByOwner(
                            requestId,
                            currentUser
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "decisionSuccess",
                            "Request approved successfully."
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

        return "redirect:/user/notifications";
    }

    @PostMapping(
            "/user/requests/{requestId}/reject"
    )
    public String reject(
            @PathVariable Long requestId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        AppUser currentUser =
                loadCurrentUser(
                        authentication
                );

        try {

            equipmentRequestService
                    .rejectByOwner(
                            requestId,
                            currentUser
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "decisionSuccess",
                            "Request rejected successfully."
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

        return "redirect:/user/notifications";
    }

    private AppUser loadCurrentUser(
            Authentication authentication
    ) {

        return appUserRepository
                .findByEmailIgnoreCase(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user no longer exists."
                        )
                );
    }
}