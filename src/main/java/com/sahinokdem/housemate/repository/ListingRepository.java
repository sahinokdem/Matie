package com.sahinokdem.housemate.repository;

import com.sahinokdem.housemate.domain.listing.Listing;
import com.sahinokdem.housemate.domain.listing.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {

    /**
     * Find an active listing by ID (not soft deleted).
     */
    Optional<Listing> findByIdAndStatusAndDeletedAtIsNull(UUID id, ListingStatus status);

    /**
     * Find all active listings by city with pagination.
     */
    Page<Listing> findAllByCityAndStatusAndDeletedAtIsNull(String city, ListingStatus status, Pageable pageable);

    /**
     * Find all active listings with pagination (no city filter).
     */
    Page<Listing> findAllByStatusAndDeletedAtIsNull(ListingStatus status, Pageable pageable);

    /**
     * Find listing by ID with owner and photos eagerly loaded (avoid N+1).
     */
    @Query("SELECT l FROM Listing l " +
           "LEFT JOIN FETCH l.owner " +
           "LEFT JOIN FETCH l.photos " +
           "WHERE l.id = :id AND l.status = :status AND l.deletedAt IS NULL")
    Optional<Listing> findByIdWithOwnerAndPhotos(@Param("id") UUID id, @Param("status") ListingStatus status);
}
