package com.kitloop.dto;

import com.kitloop.entity.EquipmentCategory;
import com.kitloop.entity.EquipmentCondition;
import com.kitloop.entity.ListingType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class EquipmentListingForm {

    private MultipartFile image;

    @NotBlank(message = "Title is required.")
    @Size(
            min = 3,
            max = 120,
            message = "Title must be between 3 and 120 characters."
    )
    private String title;

    @NotNull(message = "Category is required.")
    private EquipmentCategory category;

    @NotNull(message = "Condition is required.")
    private EquipmentCondition condition;

    @NotNull(message = "Listing type is required.")
    private ListingType listingType;

    @DecimalMin(
            value = "0.01",
            message = "Rent price must be greater than 0."
    )
    private BigDecimal rentPrice;

    @DecimalMin(
            value = "0.01",
            message = "Sale price must be greater than 0."
    )
    private BigDecimal salePrice;

    @DecimalMin(
            value = "0.01",
            message = "Refundable deposit must be greater than 0."
    )
    private BigDecimal refundableDeposit;

    @NotBlank(message = "Description is required.")
    @Size(
            min = 10,
            max = 2000,
            message = "Description must be between 10 and 2000 characters."
    )
    private String description;

    public EquipmentListingForm() {
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(
            MultipartFile image
    ) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title
    ) {
        this.title = title;
    }

    public EquipmentCategory getCategory() {
        return category;
    }

    public void setCategory(
            EquipmentCategory category
    ) {
        this.category = category;
    }

    public EquipmentCondition getCondition() {
        return condition;
    }

    public void setCondition(
            EquipmentCondition condition
    ) {
        this.condition = condition;
    }

    public ListingType getListingType() {
        return listingType;
    }

    public void setListingType(
            ListingType listingType
    ) {
        this.listingType = listingType;
    }

    public BigDecimal getRentPrice() {
        return rentPrice;
    }

    public void setRentPrice(
            BigDecimal rentPrice
    ) {
        this.rentPrice = rentPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(
            BigDecimal salePrice
    ) {
        this.salePrice = salePrice;
    }

    public BigDecimal getRefundableDeposit() {
        return refundableDeposit;
    }

    public void setRefundableDeposit(
            BigDecimal refundableDeposit
    ) {
        this.refundableDeposit = refundableDeposit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }
}