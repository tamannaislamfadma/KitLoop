package com.kitloop.service;

import com.kitloop.dto.EquipmentListingForm;
import com.kitloop.entity.AppUser;
import com.kitloop.entity.EquipmentCategory;
import com.kitloop.entity.EquipmentCondition;
import com.kitloop.entity.EquipmentListing;
import com.kitloop.entity.ListingPaymentStatus;
import com.kitloop.entity.ListingStatus;
import com.kitloop.entity.ListingType;
import com.kitloop.repository.EquipmentListingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class EquipmentListingService {

    private final EquipmentListingRepository
            equipmentListingRepository;

    private final EquipmentImageStorageService
            equipmentImageStorageService;

    public EquipmentListingService(
            EquipmentListingRepository equipmentListingRepository,
            EquipmentImageStorageService equipmentImageStorageService
    ) {
        this.equipmentListingRepository =
                equipmentListingRepository;

        this.equipmentImageStorageService =
                equipmentImageStorageService;
    }

    @Transactional
    public EquipmentListing createListing(
            AppUser owner,
            EquipmentListingForm form
    ) {

        String storedFilename =
                equipmentImageStorageService
                        .storeEquipmentImage(
                                form.getImage()
                        );

        try {

            EquipmentListing listing =
                    new EquipmentListing();

            listing.setOwner(owner);

            listing.setTitle(
                    form.getTitle().trim()
            );

            listing.setCategory(
                    form.getCategory()
            );

            listing.setCondition(
                    form.getCondition()
            );

            listing.setListingType(
                    form.getListingType()
            );

            listing.setDescription(
                    form.getDescription().trim()
            );

            listing.setImageFilename(
                    storedFilename
            );

            configurePrices(
                    listing,
                    form
            );

            listing.setStatus(
                    determineInitialStatus(
                            form.getListingType()
                    )
            );

            listing.setPaymentStatus(
                    ListingPaymentStatus.NOT_APPLICABLE
            );

            return equipmentListingRepository
                    .save(listing);

        } catch (RuntimeException exception) {

            equipmentImageStorageService
                    .deleteEquipmentImage(
                            storedFilename
                    );

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public EquipmentListing getListing(
            Long listingId
    ) {

        return equipmentListingRepository
                .findById(listingId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Equipment listing was not found."
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<EquipmentListing>
    getListingsForOwner(
            Long ownerId
    ) {

        return equipmentListingRepository
                .findByOwner_IdOrderByCreatedAtDesc(
                        ownerId
                );
    }

    @Transactional(readOnly = true)
    public List<EquipmentListing>
    searchMarketplace(
            String query,
            EquipmentCategory category,
            ListingType listingType,
            EquipmentCondition condition,
            ListingStatus status
    ) {

        String normalizedQuery =
                normalizeSearchText(query);

        return equipmentListingRepository
                .searchMarketplace(
                        normalizedQuery,
                        category,
                        listingType,
                        condition,
                        status
                );
    }

    public boolean isOwner(
            EquipmentListing listing,
            AppUser user
    ) {

        return listing
                .getOwner()
                .getId()
                .equals(
                        user.getId()
                );
    }

    private void configurePrices(
            EquipmentListing listing,
            EquipmentListingForm form
    ) {

        listing.setRentPrice(null);
        listing.setSalePrice(null);
        listing.setRefundableDeposit(null);

        if (form.getListingType()
                == ListingType.RENT) {

            listing.setRentPrice(
                    money(
                            form.getRentPrice()
                    )
            );

            return;
        }

        if (form.getListingType()
                == ListingType.SALE) {

            listing.setSalePrice(
                    money(
                            form.getSalePrice()
                    )
            );

            return;
        }

        if (form.getListingType()
                == ListingType.FREE_SHARE) {

            listing.setRefundableDeposit(
                    money(
                            form.getRefundableDeposit()
                    )
            );
        }
    }

    private ListingStatus determineInitialStatus(
            ListingType listingType
    ) {

        if (listingType
                == ListingType.RENT) {

            return ListingStatus.OPEN_FOR_RENT;
        }

        return ListingStatus.ACTIVE;
    }

    private BigDecimal money(
            BigDecimal value
    ) {

        if (value == null) {
            return null;
        }

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private String normalizeSearchText(
            String query
    ) {

        if (query == null) {
            return null;
        }

        String trimmed =
                query.trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed;
    }
}