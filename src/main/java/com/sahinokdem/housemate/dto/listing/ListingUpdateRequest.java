package com.sahinokdem.housemate.dto.listing;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingUpdateRequest {

    @Size(min = 10, max = 200, message = "Title must be between 10 and 200 characters")
    private String title;

    @Size(min = 50, max = 2000, message = "Description must be between 50 and 2000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Rent amount must be positive")
    @Digits(integer = 8, fraction = 2, message = "Invalid rent amount format")
    private BigDecimal rentAmount;

    @Future(message = "Available from date must be in the future")
    private LocalDate availableFrom;

    private Boolean furnished;

    private Boolean utilitiesIncluded;

    private Boolean petsAllowed;

    private Boolean smokingAllowed;
}
