package com.sahinokdem.housemate.repository;

import com.sahinokdem.housemate.domain.application.Application;
import com.sahinokdem.housemate.domain.application.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    boolean existsByListingIdAndApplicantIdAndStatusIn(UUID listingId, UUID applicantId, List<ApplicationStatus> statuses);

    @EntityGraph(attributePaths = {"listing", "listing.owner", "applicant"})
    Page<Application> findAllByListingOwnerId(UUID ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"listing", "listing.owner", "applicant"})
    Page<Application> findAllByApplicantId(UUID applicantId, Pageable pageable);

    @EntityGraph(attributePaths = {"listing", "listing.owner", "applicant"})
    Page<Application> findAllByApplicantIdAndStatus(UUID applicantId, ApplicationStatus status, Pageable pageable);
}
