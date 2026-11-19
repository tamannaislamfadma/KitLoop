package com.kitloop.controller;

import com.kitloop.dto.UserDashboardStats;
import com.kitloop.entity.AppUser;
import com.kitloop.entity.EquipmentCategory;
import com.kitloop.entity.EquipmentCondition;
import com.kitloop.entity.EquipmentListing;
import com.kitloop.entity.ListingStatus;
import com.kitloop.entity.ListingType;
import com.kitloop.repository.AppUserRepository;
import com.kitloop.service.EquipmentListingService;
import com.kitloop.service.TrustScoreService;
import com.kitloop.service.UserDashboardService;
import com.kitloop.service.WalletService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserPortalController {

    private final AppUserRepository
            appUserRepository;

    private final UserDashboardService
            userDashboardService;

    private final EquipmentListingService
            equipmentListingService;

    private final WalletService
            walletService;

    private final TrustScoreService
            trustScoreService;

    public UserPortalController(
            AppUserRepository appUserRepository,
            UserDashboardService userDashboardService,
            EquipmentListingService equipmentListingService,
            WalletService walletService,
            TrustScoreService trustScoreService
    ) {
        this.appUserRepository =
                appUserRepository;

        this.userDashboardService =
                userDashboardService;

        this.equipmentListingService =
                equipmentListingService;

        this.walletService =
                walletService;

        this.trustScoreService =
                trustScoreService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model
    ) {

        AppUser currentUser =
                loadCurrentUser(
                        authentication
                );

        UserDashboardStats stats =
                userDashboardService
                        .getStatsForUser(
                                currentUser.getId()
                        );

        prepareCommonModel(
                model,
                currentUser,
                "dashboard",
                "Dashboard",
                "Your KitLoop activity at a glance."
        );

        model.addAttribute(
                "stats",
                stats
        );

        model.addAttribute(
                "walletTransactions",
                walletService
                        .getRecentHistory(
                                currentUser.getId()
                        )
        );

        model.addAttribute(
                "trustHistory",
                trustScoreService
                        .getRecentHistory(
                                currentUser.getId()
                        )
        );

        return "user/dashboard";
    }

    @GetMapping("/equipments")
    public String browseEquipments(
            Authentication authentication,
            @RequestParam(
                    name = "q",
                    required = false
            )
            String query,
            @RequestParam(
                    name = "category",
                    required = false
            )
            EquipmentCategory category,
            @RequestParam(
                    name = "type",
                    required = false
            )
            ListingType listingType,
            @RequestParam(
                    name = "condition",
                    required = false
            )
            EquipmentCondition condition,
            @RequestParam(
                    name = "status",
                    required = false
            )
            ListingStatus status,
            Model model
    ) {

        AppUser currentUser =
                loadCurrentUser(
                        authentication
                );

        List<EquipmentListing> listings =
                equipmentListingService
                        .searchMarketplace(
                                query,
                                category,
                                listingType,
                                condition,
                                status
                        );

        prepareCommonModel(
                model,
                currentUser,
                "browse",
                "Browse Equipments",
                "Discover useful equipment shared by the KitLoop community."
        );

        model.addAttribute(
                "listings",
                listings
        );

        model.addAttribute(
                "categories",
                EquipmentCategory.values()
        );

        model.addAttribute(
                "listingTypes",
                ListingType.values()
        );

        model.addAttribute(
                "conditions",
                EquipmentCondition.values()
        );

        model.addAttribute(
                "statuses",
                ListingStatus.values()
        );

        model.addAttribute(
                "searchQuery",
                query
        );

        model.addAttribute(
                "selectedCategory",
                category
        );

        model.addAttribute(
                "selectedType",
                listingType
        );

        model.addAttribute(
                "selectedCondition",
                condition
        );

        model.addAttribute(
                "selectedStatus",
                status
        );

        return "user/browse-equipments";
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