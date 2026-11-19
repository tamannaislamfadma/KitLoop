package com.kitloop.controller;

import com.kitloop.entity.AppUser;
import com.kitloop.exception.RequestFlowException;
import com.kitloop.repository.AppUserRepository;
import com.kitloop.service.ListingLifecycleService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ListingLifecycleController {

    private final AppUserRepository
            appUserRepository;

    private final ListingLifecycleService
            listingLifecycleService;

    public ListingLifecycleController(
            AppUserRepository appUserRepository,
            ListingLifecycleService listingLifecycleService
    ) {
        this.appUserRepository =
                appUserRepository;

        this.listingLifecycleService =
                listingLifecycleService;
    }

    @PostMapping(
            "/user/listings/{listingId}/rent-returned"
    )
    public String rentalReturned(
            @PathVariable
            Long listingId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        AppUser owner =
                loadUser(
                        authentication
                );

        try {

            listingLifecycleService
                    .reopenRental(
                            listingId,
                            owner
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "lifecycleSuccess",
                            "Rental completed. Listing is OPEN_FOR_RENT again and you earned 4 trust points."
                    );

        } catch (
                RequestFlowException exception
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "lifecycleError",
                            exception.getMessage()
                    );
        }

        return "redirect:/user/listings";
    }

    @PostMapping(
            "/user/listings/{listingId}/share-returned"
    )
    public String freeShareReturned(
            @PathVariable
            Long listingId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        AppUser owner =
                loadUser(
                        authentication
                );

        try {

            listingLifecycleService
                    .markFreeShareReturned(
                            listingId,
                            owner
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "lifecycleSuccess",
                            "Return confirmed. Owner earned 6 points and borrower earned 5 points."
                    );

        } catch (
                RequestFlowException exception
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "lifecycleError",
                            exception.getMessage()
                    );
        }

        return "redirect:/user/listings";
    }

    private AppUser loadUser(
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