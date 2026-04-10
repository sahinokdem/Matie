package com.sahinokdem.housemate.dto.listing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class RoommateWantedUpdateRequest {

    @Size(min = 10, max = 200, message = "Title must be between 10 and 200 characters")
    private String title;

    @Size(min = 50, max = 2000, message = "Description must be between 50 and 2000 characters")
    private String description;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Pattern(regexp = "^$|^[A-Za-z]{3}$", message = "Currency must be a 3-letter code")
    private String currency;

    @DecimalMin(value = "0.0", inclusive = false, message = "Rent amount must be positive")
    private BigDecimal rentAmount;

    @Future(message = "Available from date must be in the future")
    private LocalDate availableFrom;

    private Boolean petsAllowed;

    private Boolean smokingAllowed;
}
