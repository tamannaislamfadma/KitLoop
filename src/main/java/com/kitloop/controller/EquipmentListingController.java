package com.kitloop.controller;

import com.kitloop.dto.EquipmentListingForm;
import com.kitloop.entity.AppUser;
import com.kitloop.entity.EquipmentCategory;
import com.kitloop.entity.EquipmentCondition;
import com.kitloop.entity.EquipmentListing;
import com.kitloop.entity.ListingType;
import com.kitloop.exception.FileStorageException;
import com.kitloop.repository.AppUserRepository;
import com.kitloop.service.EquipmentListingService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/user/listings")
public class EquipmentListingController {

    private final AppUserRepository
            appUserRepository;

    private final EquipmentListingService
            equipmentListingService;

    public EquipmentListingController(
            AppUserRepository appUserRepository,
            EquipmentListingService equipmentListingService
    ) {
        this.appUserRepository =
                appUserRepository;

        this.equipmentListingService =
                equipmentListingService;
    }

    @GetMapping
    public String myListings(
            Authentication authentication,
            Model model
    ) {

        AppUser currentUser =
                loadCurrentUser(
                        authentication
                );

        List<EquipmentListing> listings =
                equipmentListingService
                        .getListingsForOwner(
                                currentUser.getId()
                        );

        prepareCommonModel(
                model,
                currentUser,
                "listings",
                "My Listing",
                "Manage the equipment you make available on KitLoop."
        );

        model.addAttribute(
                "listings",
                listings
        );

        return "user/my-listings";
    }

    @GetMapping("/new")
    public String addListingPage(
            Authentication authentication,
            Model model
    ) {

        AppUser currentUser =
                loadCurrentUser(
                        authentication
                );

        prepareCommonModel(
                model,
                currentUser,
                "listings",
                "Add Equipment",
                "Create a professional KitLoop marketplace listing."
        );

        if (!model.containsAttribute(
                "listingForm"
        )) {
            model.addAttribute(
                    "listingForm",
                    new EquipmentListingForm()
            );
        }

        prepareListingFormOptions(
                model
        );

        return "user/add-equipment";
    }

    @PostMapping("/new")
    public String createListing(
            Authentication authentication,
            @Valid
            @ModelAttribute("listingForm")
            EquipmentListingForm listingForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        AppUser currentUser =
                loadCurrentUser(
                        authentication
                );

        validateConditionalFields(
                listingForm,
                bindingResult
        );

        if (listingForm.getImage() == null
                || listingForm.getImage()
                .isEmpty()) {

            bindingResult.rejectValue(
                    "image",
                    "image.required",
                    "Equipment picture is required."
            );
        }

        if (bindingResult.hasErrors()) {

            prepareCommonModel(
                    model,
                    currentUser,
                    "listings",
                    "Add Equipment",
                    "Create a professional KitLoop marketplace listing."
            );

            prepareListingFormOptions(
                    model
            );

            return "user/add-equipment";
        }

        EquipmentListing listing;

        try {

            listing =
                    equipmentListingService
                            .createListing(
                                    currentUser,
                                    listingForm
                            );

        } catch (
                FileStorageException exception
        ) {

            bindingResult.rejectValue(
                    "image",
                    "image.invalid",
                    exception.getMessage()
            );

            prepareCommonModel(
                    model,
                    currentUser,
                    "listings",
                    "Add Equipment",
                    "Create a professional KitLoop marketplace listing."
            );

            prepareListingFormOptions(
                    model
            );

            return "user/add-equipment";
        }

        redirectAttributes.addFlashAttribute(
                "listingCreated",
                true
        );

        return "redirect:/user/listings/"
                + listing.getId();
    }

    @GetMapping("/{listingId}")
    public String listingDetails(
            @PathVariable
            Long listingId,
            Authentication authentication,
            Model model
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

        boolean owner =
                equipmentListingService
                        .isOwner(
                                listing,
                                currentUser
                        );

        prepareCommonModel(
                model,
                currentUser,
                owner
                        ? "listings"
                        : "browse",
                "Equipment Details",
                "View complete equipment and owner information."
        );

        model.addAttribute(
                "listing",
                listing
        );

        model.addAttribute(
                "isOwner",
                owner
        );

        return "user/listing-details";
    }


    public String requestListing(
            @PathVariable
            Long listingId,
            Authentication authentication,
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

        if (equipmentListingService
                .isOwner(
                        listing,
                        currentUser
                )) {

            redirectAttributes.addFlashAttribute(
                    "actionError",
                    "You cannot send a request for your own equipment."
            );

            return "redirect:/user/listings/"
                    + listingId;
        }

        redirectAttributes.addFlashAttribute(
                "actionInfo",
                "The request and payment workflow will open here in Phase 5."
        );

        return "redirect:/user/listings/"
                + listingId;
    }

    @GetMapping("/{listingId}/chat")
    public String openChat(
            @PathVariable
            Long listingId,
            Authentication authentication,
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

        if (equipmentListingService
                .isOwner(
                        listing,
                        currentUser
                )) {

            redirectAttributes.addFlashAttribute(
                    "actionError",
                    "You cannot start a chat with yourself."
            );

            return "redirect:/user/listings/"
                    + listingId;
        }

        redirectAttributes.addFlashAttribute(
                "actionInfo",
                "Database-backed listing chat will be activated in the Chat phase."
        );

        return "redirect:/user/listings/"
                + listingId;
    }

    private void validateConditionalFields(
            EquipmentListingForm form,
            BindingResult bindingResult
    ) {

        if (form.getListingType() == null) {
            return;
        }

        if (form.getListingType()
                == ListingType.RENT) {

            if (!isPositive(
                    form.getRentPrice()
            )) {

                bindingResult.rejectValue(
                        "rentPrice",
                        "rentPrice.required",
                        "Rent price is required for RENT listings."
                );
            }

            return;
        }

        if (form.getListingType()
                == ListingType.SALE) {

            if (!isPositive(
                    form.getSalePrice()
            )) {

                bindingResult.rejectValue(
                        "salePrice",
                        "salePrice.required",
                        "Sale price is required for SALE listings."
                );
            }

            return;
        }

        if (form.getListingType()
                == ListingType.FREE_SHARE) {

            if (!isPositive(
                    form.getRefundableDeposit()
            )) {

                bindingResult.rejectValue(
                        "refundableDeposit",
                        "refundableDeposit.required",
                        "Refundable deposit is required for FREE SHARE listings."
                );
            }
        }
    }

    private boolean isPositive(
            BigDecimal value
    ) {

        return value != null
                && value.compareTo(
                BigDecimal.ZERO
        ) > 0;
    }

    private void prepareListingFormOptions(
            Model model
    ) {

        model.addAttribute(
                "categories",
                EquipmentCategory.values()
        );

        model.addAttribute(
                "conditions",
                EquipmentCondition.values()
        );

        model.addAttribute(
                "listingTypes",
                ListingType.values()
        );
    }

    private void prepareCommonModel(
            Model model,
            AppUser currentUser,
            String activePage,
            String pageTitle,
            String pageSubtitle
    ) {

        model.addAttribute(
                "currentUser",
                currentUser
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

    private AppUser loadCurrentUser(
            Authentication authentication
    ) {

        if (authentication == null) {
            throw new IllegalStateException(
                    "Authentication is required."
            );
        }

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