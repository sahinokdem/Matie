package com.sahinokdem.housemate.service;

import com.sahinokdem.housemate.domain.listing.Listing;
import com.sahinokdem.housemate.domain.listing.ListingPhoto;
import com.sahinokdem.housemate.domain.listing.ListingStatus;
import com.sahinokdem.housemate.domain.listing.ListingType;
import com.sahinokdem.housemate.domain.university.University;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
        private final UUID currentUserId = UUID.fromString("99999999-9999-9999-9999-999999999999");

    @BeforeEach
    void setUp() {
        listingService = new ListingService(listingRepository, userRepository, photoStorageService);
    }

    @Test
    void getListingById_returnsPhotosSortedByDisplayOrder() {
        University university = University.builder()
                .name("İzmir Yüksek Teknoloji Enstitüsü")
                .shortName("IYTE")
                .domain("iyte.edu.tr")
                .active(true)
                .build();
        university.setId(UUID.fromString("33333333-3333-3333-3333-333333333333"));

        User owner = User.builder()
                .university(university)
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

    @Test
    void getAllListings_returnsOnlySameUniversityListings() {
        UUID iYTEId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID egeId = UUID.fromString("44444444-4444-4444-4444-444444444444");

        University iYTE = University.builder()
                .name("İzmir Yüksek Teknoloji Enstitüsü")
                .shortName("IYTE")
                .domain("iyte.edu.tr")
                .active(true)
                .build();
        iYTE.setId(iYTEId);

        University ege = University.builder()
                .name("Ege Üniversitesi")
                .shortName("EGE")
                .domain("ege.edu.tr")
                .active(true)
                .build();
        ege.setId(egeId);

        User currentUser = User.builder()
                .university(iYTE)
                .email("current@mail.com")
                .passwordHash("hash")
                .firstName("Current")
                .lastName("User")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        currentUser.setId(currentUserId);

        User sameUniversityOwner = User.builder()
                .university(iYTE)
                .email("owner1@mail.com")
                .passwordHash("hash")
                .firstName("Same")
                .lastName("University")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        sameUniversityOwner.setId(UUID.fromString("55555555-5555-5555-5555-555555555555"));

        User differentUniversityOwner = User.builder()
                .university(ege)
                .email("owner2@mail.com")
                .passwordHash("hash")
                .firstName("Different")
                .lastName("University")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        differentUniversityOwner.setId(UUID.fromString("66666666-6666-6666-6666-666666666666"));

        Listing sameUniversityListing = Listing.builder()
                .owner(sameUniversityOwner)
                .listingType(ListingType.ROOM_AVAILABLE)
                .title("IYTE listing")
                .description("Same university listing should be visible")
                .address("IYTE Campus")
                .rentAmount(new BigDecimal("12000"))
                .currency("TRY")
                .availableFrom(LocalDate.now().plusDays(3))
                .status(ListingStatus.ACTIVE)
                .build();
        sameUniversityListing.setId(UUID.fromString("77777777-7777-7777-7777-777777777777"));
        sameUniversityListing.setCreatedAt(Instant.now());
        sameUniversityListing.setUpdatedAt(Instant.now());

        Listing differentUniversityListing = Listing.builder()
                .owner(differentUniversityOwner)
                .listingType(ListingType.ROOM_AVAILABLE)
                .title("EGE listing")
                .description("Different university listing should be hidden")
                .address("Bornova")
                .rentAmount(new BigDecimal("11000"))
                .currency("TRY")
                .availableFrom(LocalDate.now().plusDays(5))
                .status(ListingStatus.ACTIVE)
                .build();
        differentUniversityListing.setId(UUID.fromString("88888888-8888-8888-8888-888888888888"));

        Pageable pageable = PageRequest.of(0, 20);
        Page<Listing> filteredPage = new PageImpl<>(java.util.List.of(sameUniversityListing), pageable, 1);

        when(userRepository.findById(eq(currentUserId))).thenReturn(Optional.of(currentUser));
        when(listingRepository.findAllByOwnerUniversityIdAndStatusAndDeletedAtIsNull(eq(iYTEId), eq(ListingStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(filteredPage);

        Page<ListingResponse> response = listingService.getAllListings(currentUserId, null, pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals("IYTE listing", response.getContent().get(0).getTitle());
        assertEquals(iYTEId, response.getContent().get(0).getOwner().getUniversityId());
        assertEquals("IYTE", response.getContent().get(0).getOwner().getUniversityShortName());

        verify(listingRepository).findAllByOwnerUniversityIdAndStatusAndDeletedAtIsNull(eq(iYTEId), eq(ListingStatus.ACTIVE), any(Pageable.class));
    }
}
