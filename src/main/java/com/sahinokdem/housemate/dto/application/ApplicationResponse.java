package com.sahinokdem.housemate.dto.application;

import com.sahinokdem.housemate.domain.application.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    private UUID id;
    private UUID listingId;
    private String listingTitle;
    private UUID applicantId;
    private String applicantFirstName;
    private String applicantAvatarUrl;
    private String message;
    private ApplicationStatus status;
    private Instant createdAt;
}
