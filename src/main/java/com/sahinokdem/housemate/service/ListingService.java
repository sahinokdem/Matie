package com.sahinokdem.housemate.service;

import com.sahinokdem.housemate.domain.listing.Listing;
import com.sahinokdem.housemate.domain.listing.ListingPhoto;
import com.sahinokdem.housemate.domain.listing.ListingStatus;
import com.sahinokdem.housemate.domain.listing.ListingType;
import com.sahinokdem.housemate.domain.user.User;
import com.sahinokdem.housemate.dto.listing.*;
import com.sahinokdem.housemate.exception.BadRequestException;
import com.sahinokdem.housemate.exception.ForbiddenException;
import com.sahinokdem.housemate.exception.ResourceNotFoundException;
import com.sahinokdem.housemate.repository.ListingRepository;
import com.sahinokdem.housemate.repository.UserRepository;
import com.sahinokdem.housemate.service.storage.PhotoStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final PhotoStorageService photoStorageService;

    @Transactional
        public ListingResponse createRoomAvailableListing(UUID currentUserId, ListingCreateRequest request) {
        // Get the authenticated user
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        Listing listing = Listing.builder()
                .owner(owner)
            .listingType(ListingType.ROOM_AVAILABLE)
                .title(request.getTitle())
                .description(request.getDescription())
                .city(request.getCity())
                .address(request.getAddress())
                .postalCode(request.getPostalCode())
                .rentAmount(request.getRentAmount())
                .currency(request.getCurrency())
                .availableFrom(request.getAvailableFrom())
                .leaseDurationMonths(request.getLeaseDurationMonths())
                .roomType(request.getRoomType())
                .furnished(request.getFurnished())
                .utilitiesIncluded(request.getUtilitiesIncluded())
                .petsAllowed(request.getPetsAllowed())
                .smokingAllowed(request.getSmokingAllowed())
                .status(ListingStatus.ACTIVE)
                .build();
                
        listing.setCreatedAt(Instant.now());
        listing.setUpdatedAt(Instant.now());

        // Add photos if provided
        if (request.getPhotoUrls() != null && !request.getPhotoUrls().isEmpty()) {
            for (int i = 0; i < request.getPhotoUrls().size(); i++) {
                ListingPhoto photo = ListingPhoto.builder()
                        .photoUrl(request.getPhotoUrls().get(i))
                        .displayOrder(i)
                        .build();
                listing.addPhoto(photo);
            }
        }

        Listing savedListing = listingRepository.save(listing);
        return mapToResponse(savedListing);
    }

    @Transactional
    public ListingResponse createRoommateWantedListing(UUID currentUserId, RoommateWantedCreateRequest request) {
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        Listing listing = Listing.builder()
                .owner(owner)
                .listingType(ListingType.ROOMMATE_WANTED)
                .title(request.getTitle())
                .description(request.getDescription())
                .city(request.getCity())
            .currency(normalizeCurrency(request.getCurrency()))
            .rentAmount(request.getRentAmount())
                .availableFrom(request.getAvailableFrom())
                .petsAllowed(request.getPetsAllowed())
                .smokingAllowed(request.getSmokingAllowed())
                .status(ListingStatus.ACTIVE)
                .build();

        listing.setCreatedAt(Instant.now());
        listing.setUpdatedAt(Instant.now());

        Listing savedListing = listingRepository.save(listing);
        return mapToResponse(savedListing);
    }

    @Transactional(readOnly = true)
    public ListingResponse getListingById(UUID listingId) {
        Listing listing = listingRepository.findByIdWithOwnerAndPhotos(listingId, ListingStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + listingId));

        return mapToResponse(listing);
    }

    @Transactional(readOnly = true)
    public Page<ListingResponse> getAllListings(String city, String typeFilter, Pageable pageable) {
        Page<Listing> listings;
        ListingType listingType = parseListingTypeFilter(typeFilter);

        boolean hasCity = city != null && !city.isBlank();
        boolean hasType = listingType != null;

        if (hasCity && hasType) {
            listings = listingRepository.findAllByCityAndListingTypeAndStatusAndDeletedAtIsNull(
                    city, listingType, ListingStatus.ACTIVE, pageable
            );
        } else if (hasCity) {
            listings = listingRepository.findAllByCityAndStatusAndDeletedAtIsNull(
                    city, ListingStatus.ACTIVE, pageable
            );
        } else if (hasType) {
            listings = listingRepository.findAllByListingTypeAndStatusAndDeletedAtIsNull(
                    listingType, ListingStatus.ACTIVE, pageable
            );
        } else {
            listings = listingRepository.findAllByStatusAndDeletedAtIsNull(
                    ListingStatus.ACTIVE, pageable
            );
        }

        return listings.map(this::mapToResponse);
    }

    @Transactional
    public ListingResponse updateListing(UUID currentUserId, UUID listingId, ListingUpdateRequest request) {
        Listing listing = listingRepository.findByIdAndStatusAndDeletedAtIsNull(listingId, ListingStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + listingId));

        // CRITICAL SECURITY: Verify ownership
        if (!listing.getOwner().getId().equals(currentUserId)) {
            throw new ForbiddenException("You are not authorized to update this listing");
        }

        if (listing.getListingType() == ListingType.ROOMMATE_WANTED) {
            throw new BadRequestException("Use roommate-wanted update endpoint for ROOMMATE_WANTED listings");
        }

        // Update fields if provided
        if (request.getTitle() != null) {
            listing.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            listing.setDescription(request.getDescription());
        }
        if (request.getRentAmount() != null) {
            listing.setRentAmount(request.getRentAmount());
        }
        if (request.getAvailableFrom() != null) {
            listing.setAvailableFrom(request.getAvailableFrom());
        }
        if (request.getFurnished() != null) {
            listing.setFurnished(request.getFurnished());
        }
        if (request.getUtilitiesIncluded() != null) {
            listing.setUtilitiesIncluded(request.getUtilitiesIncluded());
        }
        if (request.getPetsAllowed() != null) {
            listing.setPetsAllowed(request.getPetsAllowed());
        }
        if (request.getSmokingAllowed() != null) {
            listing.setSmokingAllowed(request.getSmokingAllowed());
        }

        Listing updatedListing = listingRepository.save(listing);
        return mapToResponse(updatedListing);
    }

    @Transactional
    public ListingResponse updateRoommateWantedListing(UUID currentUserId, UUID listingId, RoommateWantedUpdateRequest request) {
        Listing listing = listingRepository.findByIdAndStatusAndDeletedAtIsNull(listingId, ListingStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + listingId));

        if (!listing.getOwner().getId().equals(currentUserId)) {
            throw new ForbiddenException("You are not authorized to update this listing");
        }

        if (listing.getListingType() != ListingType.ROOMMATE_WANTED) {
            throw new BadRequestException("This endpoint only supports ROOMMATE_WANTED listings");
        }

        if (request.getTitle() != null) {
            listing.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            listing.setDescription(request.getDescription());
        }
        if (request.getCity() != null) {
            listing.setCity(request.getCity());
        }
        if (request.getCurrency() != null) {
            listing.setCurrency(normalizeCurrency(request.getCurrency()));
        }
        if (request.getRentAmount() != null) {
            listing.setRentAmount(request.getRentAmount());
        }
        if (request.getAvailableFrom() != null) {
            listing.setAvailableFrom(request.getAvailableFrom());
        }
        if (request.getPetsAllowed() != null) {
            listing.setPetsAllowed(request.getPetsAllowed());
        }
        if (request.getSmokingAllowed() != null) {
            listing.setSmokingAllowed(request.getSmokingAllowed());
        }

        Listing updatedListing = listingRepository.save(listing);
        return mapToResponse(updatedListing);
    }

    @Transactional
    public void softDeleteListing(UUID currentUserId, UUID listingId) {
        Listing listing = listingRepository.findByIdAndStatusAndDeletedAtIsNull(listingId, ListingStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + listingId));

        // CRITICAL SECURITY: Verify ownership
        if (!listing.getOwner().getId().equals(currentUserId)) {
            throw new ForbiddenException("You are not authorized to delete this listing");
        }

        // Perform soft delete - DO NOT call repository.delete()
        listing.setStatus(ListingStatus.DELETED);
        listing.setDeletedAt(Instant.now());
        listing.setDeletedBy(currentUserId);

        listingRepository.save(listing);
    }

    @Transactional
    public ListingPhotoResponse addPhotoToListing(UUID currentUserId, UUID listingId, String photoUrl) {
        Listing listing = listingRepository.findByIdAndStatusAndDeletedAtIsNull(listingId, ListingStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + listingId));

        if (!listing.getOwner().getId().equals(currentUserId)) {
            throw new ForbiddenException("You are not authorized to update photos for this listing");
        }

        ListingPhoto photo = ListingPhoto.builder()
                .photoUrl(photoUrl)
                .displayOrder(listing.getPhotos().size())
                .build();

        listing.addPhoto(photo);
        Listing savedListing = listingRepository.save(listing);

        ListingPhoto savedPhoto = savedListing.getPhotos().stream()
                .filter(item -> photoUrl.equals(item.getPhotoUrl()) && item.getDisplayOrder().equals(photo.getDisplayOrder()))
                .reduce((first, second) -> second)
                .orElse(photo);

        return mapPhotoResponse(savedPhoto);
    }

    @Transactional
    public void removePhotoFromListing(UUID currentUserId, UUID listingId, UUID photoId) {
        Listing listing = listingRepository.findByIdAndStatusAndDeletedAtIsNull(listingId, ListingStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + listingId));

        if (!listing.getOwner().getId().equals(currentUserId)) {
            throw new ForbiddenException("You are not authorized to update photos for this listing");
        }

        ListingPhoto photo = listing.getPhotos().stream()
                .filter(item -> item.getId().equals(photoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        String fileUrl = photo.getPhotoUrl();
        listing.removePhoto(photo);
        listingRepository.save(listing);

        try {
            photoStorageService.deletePhoto(fileUrl);
        } catch (RuntimeException ignored) {
        }
    }

    private ListingResponse mapToResponse(Listing listing) {
        ListingOwnerResponse ownerResponse = ListingOwnerResponse.builder()
                .id(listing.getOwner().getId())
                .firstName(listing.getOwner().getFirstName())
                .lastName(listing.getOwner().getLastName())
                .avatarUrl(listing.getOwner().getAvatarUrl())
                .build();

        List<ListingPhotoResponse> photoResponses = listing.getPhotos().stream()
            .map(this::mapPhotoResponse)
                .collect(Collectors.toList());

        return ListingResponse.builder()
                .id(listing.getId())
                .listingType(listing.getListingType())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .city(listing.getCity())
                .address(listing.getAddress())
                .postalCode(listing.getPostalCode())
                .rentAmount(listing.getRentAmount())
                .currency(listing.getCurrency())
                .availableFrom(listing.getAvailableFrom())
                .leaseDurationMonths(listing.getLeaseDurationMonths())
                .roomType(listing.getRoomType())
                .furnished(listing.getFurnished())
                .utilitiesIncluded(listing.getUtilitiesIncluded())
                .petsAllowed(listing.getPetsAllowed())
                .smokingAllowed(listing.getSmokingAllowed())
                .status(listing.getStatus())
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .owner(ownerResponse)
                .photos(photoResponses)
                .build();
    }

    private ListingPhotoResponse mapPhotoResponse(ListingPhoto photo) {
        return ListingPhotoResponse.builder()
                .id(photo.getId())
                .photoUrl(photo.getPhotoUrl())
                .displayOrder(photo.getDisplayOrder())
                .build();
    }

    private ListingType parseListingTypeFilter(String typeFilter) {
        if (typeFilter == null || typeFilter.isBlank() || "ALL".equalsIgnoreCase(typeFilter)) {
            return null;
        }

        try {
            return ListingType.valueOf(typeFilter.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid type filter. Allowed values: ALL, ROOM_AVAILABLE, ROOMMATE_WANTED");
        }
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "TRY";
        }
        return currency.trim().toUpperCase();
    }
}
