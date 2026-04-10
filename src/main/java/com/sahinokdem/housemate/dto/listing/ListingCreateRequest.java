package com.sahinokdem.housemate.dto.listing;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 200, message = "Title must be between 10 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 2000, message = "Description must be between 50 and 2000 characters")
    private String description;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @NotNull(message = "Rent amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rent amount must be positive")
    @Digits(integer = 8, fraction = 2, message = "Invalid rent amount format")
    private BigDecimal rentAmount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
    private String currency;

    @NotNull(message = "Available from date is required")
    @Future(message = "Available from date must be in the future")
    private LocalDate availableFrom;

    @Min(value = 1, message = "Lease duration must be at least 1 month")
    @Max(value = 120, message = "Lease duration must not exceed 120 months")
    private Integer leaseDurationMonths;

    @Size(max = 50, message = "Room type must not exceed 50 characters")
    private String roomType;

    @NotNull(message = "Furnished status is required")
    private Boolean furnished;

    @NotNull(message = "Utilities included status is required")
    private Boolean utilitiesIncluded;

    @NotNull(message = "Pets allowed status is required")
    private Boolean petsAllowed;

    @NotNull(message = "Smoking allowed status is required")
    private Boolean smokingAllowed;

    @Size(max = 10, message = "Maximum 10 photos allowed")
    private List<@NotBlank @Size(max = 500) String> photoUrls;
}
