package com.sahinokdem.housemate.repository;

import com.sahinokdem.housemate.domain.chat.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByApplicationId(UUID applicationId);

    @EntityGraph(attributePaths = {"application", "application.listing", "application.listing.owner", "application.applicant"})
    Page<Conversation> findAllByApplicationListingOwnerIdOrApplicationApplicantId(UUID ownerId, UUID applicantId, Pageable pageable);

        @EntityGraph(attributePaths = {"application", "application.listing", "application.listing.owner", "application.applicant"})
        List<Conversation> findAllByApplicationListingOwnerIdOrApplicationApplicantIdOrderByUpdatedAtDesc(UUID ownerId, UUID applicantId);

    @EntityGraph(attributePaths = {"application", "application.listing", "application.listing.owner", "application.applicant"})
    Optional<Conversation> findWithParticipantsById(UUID id);

    @Query("""
            SELECT c
            FROM Conversation c
            WHERE c.application.listing.id = :listingId
              AND (
                    (c.application.listing.owner.id = :user1Id AND c.application.applicant.id = :user2Id)
                 OR (c.application.listing.owner.id = :user2Id AND c.application.applicant.id = :user1Id)
              )
            """)
    Optional<Conversation> findByListingAndUserPair(
            @Param("listingId") UUID listingId,
            @Param("user1Id") UUID user1Id,
            @Param("user2Id") UUID user2Id
    );
}
