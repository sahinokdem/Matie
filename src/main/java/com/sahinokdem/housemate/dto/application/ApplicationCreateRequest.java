package com.sahinokdem.housemate.dto.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCreateRequest {

    @NotNull(message = "Listing ID is required")
    private UUID listingId;

    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;
}
