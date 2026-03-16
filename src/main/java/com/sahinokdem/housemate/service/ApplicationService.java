package com.sahinokdem.housemate.service;

import com.sahinokdem.housemate.domain.application.Application;
import com.sahinokdem.housemate.domain.application.ApplicationStatus;
import com.sahinokdem.housemate.domain.listing.Listing;
import com.sahinokdem.housemate.domain.listing.ListingStatus;
import com.sahinokdem.housemate.domain.user.User;
import com.sahinokdem.housemate.dto.application.ApplicationCreateRequest;
import com.sahinokdem.housemate.dto.application.ApplicationResponse;
import com.sahinokdem.housemate.dto.application.ApplicationStatusUpdateRequest;
import com.sahinokdem.housemate.exception.BadRequestException;
import com.sahinokdem.housemate.exception.ForbiddenException;
import com.sahinokdem.housemate.exception.ResourceNotFoundException;
import com.sahinokdem.housemate.repository.ApplicationRepository;
import com.sahinokdem.housemate.repository.ListingRepository;
import com.sahinokdem.housemate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    @Transactional
    public ApplicationResponse createApplication(UUID currentUserId, ApplicationCreateRequest request) {
        User applicant = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        Listing listing = listingRepository.findByIdAndStatusAndDeletedAtIsNull(request.getListingId(), ListingStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + request.getListingId()));

        if (listing.getOwner().getId().equals(currentUserId)) {
            throw new BadRequestException("You cannot apply to your own listing");
        }

        boolean alreadyExists = applicationRepository.existsByListingIdAndApplicantIdAndStatusIn(
                listing.getId(),
                applicant.getId(),
                List.of(ApplicationStatus.PENDING, ApplicationStatus.ACCEPTED)
        );
        if (alreadyExists) {
            throw new BadRequestException("You already have an active application for this listing");
        }

        Application application = Application.builder()
                .listing(listing)
                .applicant(applicant)
                .message(request.getMessage())
                .status(ApplicationStatus.PENDING)
                .build();
        application.setCreatedAt(Instant.now());

        Application savedApplication = applicationRepository.save(application);
        return mapToResponse(savedApplication);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(UUID currentUserId, UUID applicationId, ApplicationStatusUpdateRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        ApplicationStatus newStatus = request.getStatus();
        if (newStatus == ApplicationStatus.ACCEPTED || newStatus == ApplicationStatus.REJECTED) {
            if (!application.getListing().getOwner().getId().equals(currentUserId)) {
                throw new ForbiddenException("You are not authorized to update this application status");
            }
        } else if (newStatus == ApplicationStatus.CANCELLED) {
            if (!application.getApplicant().getId().equals(currentUserId)) {
                throw new ForbiddenException("You are not authorized to cancel this application");
            }
        } else {
            throw new BadRequestException("Manual update to PENDING status is not allowed");
        }

        application.setStatus(newStatus);
        Application updatedApplication = applicationRepository.save(application);
        return mapToResponse(updatedApplication);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getReceivedApplications(UUID currentUserId, Pageable pageable) {
        return applicationRepository.findAllByListingOwnerId(currentUserId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getSentApplications(UUID currentUserId, Pageable pageable, String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank() || "ALL".equalsIgnoreCase(statusFilter)) {
            return applicationRepository.findAllByApplicantId(currentUserId, pageable)
                    .map(this::mapToResponse);
        }

        ApplicationStatus status;
        try {
            status = ApplicationStatus.valueOf(statusFilter.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid status filter. Allowed values: ALL, PENDING, ACCEPTED, REJECTED, CANCELLED");
        }

        return applicationRepository.findAllByApplicantIdAndStatus(currentUserId, status, pageable)
                .map(this::mapToResponse);
    }

    private ApplicationResponse mapToResponse(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .listingId(application.getListing().getId())
                .listingTitle(application.getListing().getTitle())
                .applicantId(application.getApplicant().getId())
                .applicantFirstName(application.getApplicant().getFirstName())
                .applicantAvatarUrl(application.getApplicant().getAvatarUrl())
                .message(application.getMessage())
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .build();
    }
}
