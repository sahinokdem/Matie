package com.sahinokdem.housemate.service;

import com.sahinokdem.housemate.domain.listing.Listing;
import com.sahinokdem.housemate.domain.listing.ListingPhoto;
import com.sahinokdem.housemate.domain.listing.ListingStatus;
import com.sahinokdem.housemate.domain.listing.ListingType;
import com.sahinokdem.housemate.domain.user.User;
import com.sahinokdem.housemate.domain.user.UserRole;
import com.sahinokdem.housemate.domain.user.UserStatus;
import com.sahinokdem.housemate.dto.listing.ListingResponse;
import com.sahinokdem.housemate.repository.ListingRepository;
import com.sahinokdem.housemate.repository.UserRepository;
import com.sahinokdem.housemate.service.storage.PhotoStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PhotoStorageService photoStorageService;

    private ListingService listingService;

    private final UUID listingId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID ownerId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        listingService = new ListingService(listingRepository, userRepository, photoStorageService);
    }

    @Test
    void getListingById_returnsPhotosSortedByDisplayOrder() {
        User owner = User.builder()
                .email("owner@mail.com")
                .passwordHash("hash")
                .firstName("Owner")
                .lastName("Test")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        owner.setId(ownerId);

        Listing listing = Listing.builder()
                .owner(owner)
                .listingType(ListingType.ROOM_AVAILABLE)
                .title("Room with photos")
                .description("Test listing for photo ordering")
                .city("Istanbul")
                .address("Kadikoy")
                .postalCode("34710")
                .rentAmount(new BigDecimal("15000"))
                .currency("TRY")
                .availableFrom(LocalDate.now().plusDays(7))
                .status(ListingStatus.ACTIVE)
                .build();
        listing.setId(listingId);
        listing.setCreatedAt(Instant.now());
        listing.setUpdatedAt(Instant.now());

        ListingPhoto laterPhoto = ListingPhoto.builder()
                .photoUrl("https://cdn.example.com/photo-2.jpg")
                .displayOrder(1)
                .build();
        ListingPhoto earlierPhoto = ListingPhoto.builder()
                .photoUrl("https://cdn.example.com/photo-1.jpg")
                .displayOrder(0)
                .build();

        listing.addPhoto(laterPhoto);
        listing.addPhoto(earlierPhoto);

        when(listingRepository.findByIdWithOwnerAndPhotos(eq(listingId), eq(ListingStatus.ACTIVE)))
                .thenReturn(Optional.of(listing));

        ListingResponse response = listingService.getListingById(listingId);

        assertEquals(2, response.getPhotos().size());
        assertEquals(0, response.getPhotos().get(0).getDisplayOrder());
        assertEquals("https://cdn.example.com/photo-1.jpg", response.getPhotos().get(0).getPhotoUrl());
        assertEquals(1, response.getPhotos().get(1).getDisplayOrder());
        assertEquals("https://cdn.example.com/photo-2.jpg", response.getPhotos().get(1).getPhotoUrl());
    }
}
