package com.sahinokdem.housemate.domain.listing;

import com.sahinokdem.housemate.domain.BaseEntity;
import com.sahinokdem.housemate.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Listing extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "listing_type", nullable = false)
    private ListingType listingType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "rent_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal rentAmount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "available_from", nullable = false)
    private LocalDate availableFrom;

    @Column(name = "lease_duration_months")
    private Integer leaseDurationMonths;

    @Column(name = "room_type", length = 50)
    private String roomType;

    @Column(name = "furnished", nullable = false)
    @Builder.Default
    private Boolean furnished = false;

    @Column(name = "utilities_included", nullable = false)
    @Builder.Default
    private Boolean utilitiesIncluded = false;

    @Column(name = "pets_allowed", nullable = false)
    @Builder.Default
    private Boolean petsAllowed = false;

    @Column(name = "smoking_allowed", nullable = false)
    @Builder.Default
    private Boolean smokingAllowed = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ListingStatus status = ListingStatus.ACTIVE;

    // Soft delete fields
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @Column(name = "deletion_reason", columnDefinition = "TEXT")
    private String deletionReason;

    // Relationships
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "listing", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<ListingPhoto> photos = new ArrayList<>();

    // Helper methods
    public boolean isDeleted() {
        return status == ListingStatus.DELETED;
    }

    public boolean isActive() {
        return status == ListingStatus.ACTIVE;
    }

    public void addPhoto(ListingPhoto photo) {
        photos.add(photo);
        photo.setListing(this);
    }

    public void removePhoto(ListingPhoto photo) {
        photos.remove(photo);
        photo.setListing(null);
    }

    public List<ListingPhoto> getPhotos() {
        return Collections.unmodifiableList(this.photos);
    }
}
