package com.sahinokdem.housemate.dto.listing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sahinokdem.housemate.domain.listing.ListingStatus;
import com.sahinokdem.housemate.domain.listing.ListingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListingResponse {

    private UUID id;
    private ListingType listingType;
    private String title;
    private String description;
    private String city;
    private String address;
    private String postalCode;
    private BigDecimal rentAmount;
    private String currency;
    private LocalDate availableFrom;
    private Integer leaseDurationMonths;
    private String roomType;
    private Boolean furnished;
    private Boolean utilitiesIncluded;
    private Boolean petsAllowed;
    private Boolean smokingAllowed;
    private ListingStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private ListingOwnerResponse owner;
    private List<ListingPhotoResponse> photos;
}
