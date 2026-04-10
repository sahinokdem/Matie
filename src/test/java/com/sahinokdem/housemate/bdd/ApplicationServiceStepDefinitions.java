package com.sahinokdem.housemate.bdd;

import com.sahinokdem.housemate.domain.application.Application;
import com.sahinokdem.housemate.domain.application.ApplicationStatus;
import com.sahinokdem.housemate.domain.listing.Listing;
import com.sahinokdem.housemate.domain.listing.ListingStatus;
import com.sahinokdem.housemate.domain.listing.ListingType;
import com.sahinokdem.housemate.domain.user.User;
import com.sahinokdem.housemate.domain.user.UserRole;
import com.sahinokdem.housemate.domain.user.UserStatus;
import com.sahinokdem.housemate.dto.application.ApplicationCreateRequest;
import com.sahinokdem.housemate.dto.application.ApplicationResponse;
import com.sahinokdem.housemate.dto.application.ApplicationStatusUpdateRequest;
import com.sahinokdem.housemate.exception.BadRequestException;
import com.sahinokdem.housemate.exception.ForbiddenException;
import com.sahinokdem.housemate.repository.ApplicationRepository;
import com.sahinokdem.housemate.repository.ListingRepository;
import com.sahinokdem.housemate.repository.UserRepository;
import com.sahinokdem.housemate.service.ApplicationService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationServiceStepDefinitions {

    private ApplicationRepository applicationRepository;
    private UserRepository userRepository;
    private ListingRepository listingRepository;
    private ApplicationService applicationService;

    private UUID currentUserId;
    private UUID ownerId;
    private UUID applicationId;
    private UUID listingId;

    private User currentUser;
    private User ownerUser;
    private Listing listing;
    private Application existingApplication;

    private RuntimeException caughtException;
    private ApplicationResponse applicationResponse;

    private boolean activeApplicationExists;

    @Before
    public void setUp() {
        applicationRepository = mock(ApplicationRepository.class);
        userRepository = mock(UserRepository.class);
        listingRepository = mock(ListingRepository.class);
        applicationService = new ApplicationService(applicationRepository, userRepository, listingRepository);

        currentUserId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
        listingId = UUID.randomUUID();

        currentUser = createUser(currentUserId, "Applicant");
        ownerUser = createUser(ownerId, "Owner");
        listing = createListing(listingId, ownerUser);

        caughtException = null;
        applicationResponse = null;
        activeApplicationExists = false;

        when(userRepository.findById(eq(currentUserId))).thenReturn(Optional.of(currentUser));
        when(listingRepository.findByIdAndStatusAndDeletedAtIsNull(eq(listingId), eq(ListingStatus.ACTIVE)))
                .thenReturn(Optional.of(listing));
        when(applicationRepository.existsByListingIdAndApplicantIdAndStatusIn(eq(listingId), eq(currentUserId), anyList()))
                .thenAnswer(invocation -> activeApplicationExists);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application app = invocation.getArgument(0);
            if (app.getId() == null) {
                app.setId(UUID.randomUUID());
            }
            return app;
        });
    }

    @Given("a listing owned by current user")
    public void aListingOwnedByCurrentUser() {
        listing.setOwner(currentUser);
    }

    @Given("a listing owned by another user")
    public void aListingOwnedByAnotherUser() {
        listing.setOwner(ownerUser);
    }

    @Given("an active application already exists for same listing and user")
    public void anActiveApplicationAlreadyExistsForSameListingAndUser() {
        activeApplicationExists = true;
    }

    @Given("a previous application was REJECTED")
    public void aPreviousApplicationWasRejected() {
        activeApplicationExists = false;
    }

    @Given("a previous application was CANCELLED")
    public void aPreviousApplicationWasCancelled() {
        activeApplicationExists = false;
    }

    @Given("an existing pending application")
    public void anExistingPendingApplication() {
        listing.setOwner(ownerUser);

        existingApplication = Application.builder()
                .listing(listing)
                .applicant(currentUser)
                .message("Hi")
                .status(ApplicationStatus.PENDING)
                .build();
        existingApplication.setId(applicationId);

        when(applicationRepository.findById(eq(applicationId))).thenReturn(Optional.of(existingApplication));
    }

    @When("the user creates an application for that listing")
    public void theUserCreatesAnApplicationForThatListing() {
        ApplicationCreateRequest request = ApplicationCreateRequest.builder()
                .listingId(listingId)
                .message("I am interested")
                .build();

        try {
            applicationResponse = applicationService.createApplication(currentUserId, request);
        } catch (RuntimeException ex) {
            caughtException = ex;
        }
    }

    @When("the listing owner updates the application status to ACCEPTED")
    public void theListingOwnerUpdatesTheApplicationStatusToAccepted() {
        ApplicationStatusUpdateRequest request = ApplicationStatusUpdateRequest.builder()
                .status(ApplicationStatus.ACCEPTED)
                .build();

        applicationResponse = applicationService.updateApplicationStatus(ownerId, applicationId, request);
    }

    @When("a non-owner updates the application status to REJECTED")
    public void aNonOwnerUpdatesTheApplicationStatusToRejected() {
        ApplicationStatusUpdateRequest request = ApplicationStatusUpdateRequest.builder()
                .status(ApplicationStatus.REJECTED)
                .build();

        UUID nonOwnerId = UUID.randomUUID();
        try {
            applicationService.updateApplicationStatus(nonOwnerId, applicationId, request);
        } catch (RuntimeException ex) {
            caughtException = ex;
        }
    }

    @When("the applicant updates the application status to CANCELLED")
    public void theApplicantUpdatesTheApplicationStatusToCancelled() {
        ApplicationStatusUpdateRequest request = ApplicationStatusUpdateRequest.builder()
                .status(ApplicationStatus.CANCELLED)
                .build();

        applicationResponse = applicationService.updateApplicationStatus(currentUserId, applicationId, request);
    }

    @When("the applicant manually updates the application status to PENDING")
    public void theApplicantManuallyUpdatesTheApplicationStatusToPending() {
        ApplicationStatusUpdateRequest request = ApplicationStatusUpdateRequest.builder()
                .status(ApplicationStatus.PENDING)
                .build();

        try {
            applicationService.updateApplicationStatus(currentUserId, applicationId, request);
        } catch (RuntimeException ex) {
            caughtException = ex;
        }
    }

    @Then("a bad request error is thrown containing {string}")
    public void aBadRequestErrorIsThrownContaining(String text) {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            if (caughtException != null) {
                throw caughtException;
            }
        });
        assertTrue(ex.getMessage().contains(text));
    }

    @Then("a forbidden error is thrown containing {string}")
    public void aForbiddenErrorIsThrownContaining(String text) {
        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> {
            if (caughtException != null) {
                throw caughtException;
            }
        });
        assertTrue(ex.getMessage().contains(text));
    }

    @Then("the application response status should be ACCEPTED")
    public void theApplicationResponseStatusShouldBeAccepted() {
        assertNotNull(applicationResponse);
        assertEquals(ApplicationStatus.ACCEPTED, applicationResponse.getStatus());
    }

    @Then("the application response status should be CANCELLED")
    public void theApplicationResponseStatusShouldBeCancelled() {
        assertNotNull(applicationResponse);
        assertEquals(ApplicationStatus.CANCELLED, applicationResponse.getStatus());
    }

    @Then("the application response status should be PENDING")
    public void theApplicationResponseStatusShouldBePending() {
        assertNotNull(applicationResponse);
        assertEquals(ApplicationStatus.PENDING, applicationResponse.getStatus());
    }

    private User createUser(UUID id, String firstName) {
        User user = User.builder()
                .email(firstName.toLowerCase() + "@mail.com")
                .passwordHash("hash")
                .firstName(firstName)
                .lastName("Test")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        user.setId(id);
        return user;
    }

    private Listing createListing(UUID id, User owner) {
        Listing createdListing = Listing.builder()
                .owner(owner)
                .listingType(ListingType.ROOM_AVAILABLE)
                .title("Nice room in city center")
                .description("A great room with all amenities and close to transportation.")
                .city("Istanbul")
                .status(ListingStatus.ACTIVE)
                .build();
        createdListing.setId(id);
        return createdListing;
    }
}
