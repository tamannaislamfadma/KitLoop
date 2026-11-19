package com.kitloop.controller;

import com.kitloop.dto.EquipmentRequestForm;
import com.kitloop.dto.PaymentInstructions;
import com.kitloop.entity.AppUser;
import com.kitloop.entity.EquipmentListing;
import com.kitloop.entity.EquipmentRequest;
import com.kitloop.exception.FileStorageException;
import com.kitloop.exception.RequestFlowException;
import com.kitloop.repository.AppUserRepository;
import com.kitloop.service.EquipmentListingService;
import com.kitloop.service.EquipmentRequestService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EquipmentRequestController {

    private final AppUserRepository appUserRepository;
    private final EquipmentListingService equipmentListingService;
    private final EquipmentRequestService equipmentRequestService;

    public EquipmentRequestController(
            AppUserRepository appUserRepository,
            EquipmentListingService equipmentListingService,
            EquipmentRequestService equipmentRequestService
    ) {
        this.appUserRepository = appUserRepository;
        this.equipmentListingService = equipmentListingService;
        this.equipmentRequestService = equipmentRequestService;
    }

    @GetMapping(
            "/user/listings/{listingId}/request"
    )
    public String requestPage(
            @PathVariable Long listingId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        AppUser currentUser =
                loadCurrentUser(
                        authentication
                );

        EquipmentListing listing =
                equipmentListingService
                        .getListing(
                                listingId
                        );

        PaymentInstructions instructions;

        try {

            instructions =
                    equipmentRequestService
                            .getPaymentInstructions(
                                    listing,
                                    currentUser
                            );

        } catch (
                RequestFlowException exception
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "actionError",
                            exception.getMessage()
                    );

            return "redirect:/user/listings/"
                    + listingId;
        }

        prepareCommonModel(
                model,
                currentUser,
                listing,
                instructions
        );

        model.addAttribute(
                "requestForm",
                new EquipmentRequestForm()
        );

        return "user/payment-request";
    }

    @PostMapping(
            "/user/listings/{listingId}/request"
    )
    public String submitRequest(
            @PathVariable Long listingId,
            Authentication authentication,
            @Valid
            @ModelAttribute("requestForm")
            EquipmentRequestForm requestForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        AppUser currentUser =
                loadCurrentUser(
                        authentication
                );

        EquipmentListing listing =
                equipmentListingService
                        .getListing(
                                listingId
                        );

        PaymentInstructions instructions;

        try {

            instructions =
                    equipmentRequestService
                            .getPaymentInstructions(
                                    listing,
                                    currentUser
                            );

        } catch (
                RequestFlowException exception
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "actionError",
                            exception.getMessage()
                    );

            return "redirect:/user/listings/"
                    + listingId;
        }

        if (requestForm
                .getPaymentScreenshot()
                == null
                || requestForm
                .getPaymentScreenshot()
                .isEmpty()) {

            bindingResult.rejectValue(
                    "paymentScreenshot",
                    "paymentScreenshot.required",
                    "Payment screenshot is required."
            );
        }

        if (bindingResult.hasErrors()) {

            prepareCommonModel(
                    model,
                    currentUser,
                    listing,
                    instructions
            );

            return "user/payment-request";
        }

        EquipmentRequest request;

        try {

            request =
                    equipmentRequestService
                            .submitRequest(
                                    listing,
                                    currentUser,
                                    requestForm
                            );

        } catch (
                RequestFlowException
                | FileStorageException exception
        ) {

            model.addAttribute(
                    "requestError",
                    exception.getMessage()
            );

            prepareCommonModel(
                    model,
                    currentUser,
                    listing,
                    instructions
            );

            return "user/payment-request";
        }

        redirectAttributes
                .addFlashAttribute(
                        "requestSubmitted",
                        true
                );

        return "redirect:/user/requests/"
                + request.getId();
    }

    @GetMapping(
            "/user/requests/{requestId}"
    )
    public String requestDetails(
            @PathVariable Long requestId,
            Authentication authentication,
            Model model
    ) {

        AppUser currentUser =
                loadCurrentUser(
                        authentication
                );

        EquipmentRequest request =
                equipmentRequestService
                        .getRequestForUser(
                                requestId,
                                currentUser
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
                "Request Details"
        );

        model.addAttribute(
                "pageSubtitle",
                "Review request and manual payment information."
        );

        model.addAttribute(
                "request",
                request
        );

        model.addAttribute(
                "canViewProof",
                equipmentRequestService
                        .canViewPaymentProof(
                                request,
                                currentUser
                        )
        );

        return "user/request-details";
    }

    private void prepareCommonModel(
            Model model,
            AppUser currentUser,
            EquipmentListing listing,
            PaymentInstructions instructions
    ) {

        model.addAttribute(
                "currentUser",
                currentUser
        );

        model.addAttribute(
                "activePage",
                "browse"
        );

        model.addAttribute(
                "pageTitle",
                "Send Request"
        );

        model.addAttribute(
                "pageSubtitle",
                "Submit your manual bKash payment proof."
        );

        model.addAttribute(
                "listing",
                listing
        );

        model.addAttribute(
                "paymentInstructions",
                instructions
        );
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