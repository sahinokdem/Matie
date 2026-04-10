package com.sahinokdem.housemate.bdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sahinokdem.housemate.controller.ListingController;
import com.sahinokdem.housemate.domain.listing.ListingStatus;
import com.sahinokdem.housemate.domain.listing.ListingType;
import com.sahinokdem.housemate.domain.user.User;
import com.sahinokdem.housemate.domain.user.UserRole;
import com.sahinokdem.housemate.domain.user.UserStatus;
import com.sahinokdem.housemate.dto.listing.AddListingPhotoRequest;
import com.sahinokdem.housemate.dto.listing.ListingCreateRequest;
import com.sahinokdem.housemate.dto.listing.ListingOwnerResponse;
import com.sahinokdem.housemate.dto.listing.ListingPhotoResponse;
import com.sahinokdem.housemate.dto.listing.ListingResponse;
import com.sahinokdem.housemate.dto.listing.ListingUpdateRequest;
import com.sahinokdem.housemate.dto.listing.RoommateWantedCreateRequest;
import com.sahinokdem.housemate.exception.ForbiddenException;
import com.sahinokdem.housemate.exception.GlobalExceptionHandler;
import com.sahinokdem.housemate.exception.ResourceNotFoundException;
import com.sahinokdem.housemate.security.UserDetailsImpl;
import com.sahinokdem.housemate.service.ListingService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class ListingStepDefinitions {

        private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    private MockMvc mockMvc;
    private ListingService listingService;

    private UserDetailsImpl currentUserDetails;

    private ListingCreateRequest listingCreateRequest;
    private RoommateWantedCreateRequest roommateWantedCreateRequest;
    private ListingUpdateRequest listingUpdateRequest;
    private AddListingPhotoRequest addListingPhotoRequest;

    private MvcResult lastResult;

    private final UUID listingId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID photoId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID ownerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID nonOwnerId = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Before
    public void beforeScenario() {
        listingService = org.mockito.Mockito.mock(ListingService.class);
        ListingController listingController = new ListingController(listingService);

        mockMvc = MockMvcBuilders.standaloneSetup(listingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();

        currentUserDetails = new UserDetailsImpl(buildUser(ownerId, "Owner"));
        reset(listingService);

        listingCreateRequest = null;
        roommateWantedCreateRequest = null;
        listingUpdateRequest = null;
        addListingPhotoRequest = null;
        lastResult = null;
    }

    @Given("listing API test context is initialized")
    public void listingApiTestContextIsInitialized() {
    }

    @Given("I am authenticated as listing owner")
    public void iAmAuthenticatedAsListingOwner() {
        currentUserDetails = new UserDetailsImpl(buildUser(ownerId, "Owner"));
    }

    @Given("I am authenticated as non-owner user")
    public void iAmAuthenticatedAsNonOwnerUser() {
        currentUserDetails = new UserDetailsImpl(buildUser(nonOwnerId, "NonOwner"));
    }

    @And("I have a valid room-available request payload")
    public void iHaveAValidRoomAvailableRequestPayload() {
        listingCreateRequest = ListingCreateRequest.builder()
                .title("Spacious room in city center")
                .description("Very clean room, close to metro, internet included and suitable for students.")
                .city("Istanbul")
                .address("Kadikoy, Moda Street 10")
                .postalCode("34710")
                .rentAmount(new BigDecimal("15000"))
                .currency("TRY")
                .availableFrom(LocalDate.now().plusDays(10))
                .leaseDurationMonths(12)
                .roomType("PRIVATE")
                .furnished(true)
                .utilitiesIncluded(true)
                .petsAllowed(false)
                .smokingAllowed(false)
                .photoUrls(List.of("https://cdn.example.com/room-1.jpg"))
                .build();
    }

    @And("I have a room-available request payload without address")
    public void iHaveARoomAvailableRequestPayloadWithoutAddress() {
        iHaveAValidRoomAvailableRequestPayload();
        listingCreateRequest.setAddress(null);
    }

    @And("I have a room-available request payload with invalid rentAmount")
    public void iHaveARoomAvailableRequestPayloadWithInvalidRentAmount() {
        iHaveAValidRoomAvailableRequestPayload();
        listingCreateRequest.setRentAmount(new BigDecimal("-1"));
    }

    @And("listing service returns a room-available listing response")
    public void listingServiceReturnsARoomAvailableListingResponse() {
        when(listingService.createRoomAvailableListing(eq(ownerId), any(ListingCreateRequest.class)))
                .thenReturn(buildListingResponse(ListingType.ROOM_AVAILABLE, true));
    }

    @And("I have a valid roommate-wanted request payload")
    public void iHaveAValidRoommateWantedRequestPayload() {
        roommateWantedCreateRequest = RoommateWantedCreateRequest.builder()
                .title("Looking for roommate in central area")
                .description("I am looking for a clean and respectful roommate near public transport.")
                .city("Izmir")
                .currency("TRY")
                .rentAmount(new BigDecimal("12000"))
                .availableFrom(LocalDate.now().plusDays(7))
                .petsAllowed(true)
                .smokingAllowed(false)
                .build();
    }

    @And("listing service returns a roommate-wanted listing response")
    public void listingServiceReturnsARoommateWantedListingResponse() {
        when(listingService.createRoommateWantedListing(eq(ownerId), any(RoommateWantedCreateRequest.class)))
                .thenReturn(buildListingResponse(ListingType.ROOMMATE_WANTED, false));
    }

    @And("listing service returns only roommate-wanted listings for type filter")
    public void listingServiceReturnsOnlyRoommateWantedListingsForTypeFilter() {
        List<ListingResponse> content = List.of(
                buildListingResponse(ListingType.ROOMMATE_WANTED, false),
                buildListingResponse(ListingType.ROOMMATE_WANTED, false)
        );

        when(listingService.getAllListings(eq(null), eq("ROOMMATE_WANTED"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 20), content.size()));
    }

    @And("listing service rejects room update with forbidden")
    public void listingServiceRejectsRoomUpdateWithForbidden() {
        doThrow(new ForbiddenException("You are not authorized to update this listing"))
                .when(listingService)
                .updateListing(eq(nonOwnerId), eq(listingId), any(ListingUpdateRequest.class));
    }

    @And("listing service rejects delete with forbidden")
    public void listingServiceRejectsDeleteWithForbidden() {
        doThrow(new ForbiddenException("You are not authorized to delete this listing"))
                .when(listingService)
                .softDeleteListing(eq(nonOwnerId), eq(listingId));
    }

    @And("I have a valid room update payload")
    public void iHaveAValidRoomUpdatePayload() {
        listingUpdateRequest = ListingUpdateRequest.builder()
                .title("Updated title for room")
                .description("Updated description with enough details for validation requirements in system.")
                .rentAmount(new BigDecimal("18000"))
                .availableFrom(LocalDate.now().plusDays(15))
                .furnished(true)
                .utilitiesIncluded(true)
                .petsAllowed(false)
                .smokingAllowed(false)
                .build();
    }

    @And("listing service returns a created listing photo")
    public void listingServiceReturnsACreatedListingPhoto() {
        when(listingService.addPhotoToListing(eq(ownerId), eq(listingId), eq("https://cdn.example.com/photo-1.jpg")))
                .thenReturn(ListingPhotoResponse.builder()
                        .id(photoId)
                        .photoUrl("https://cdn.example.com/photo-1.jpg")
                        .displayOrder(0)
                        .build());
    }

        @And("listing service returns listing details with ordered photos")
        public void listingServiceReturnsListingDetailsWithOrderedPhotos() {
        ListingResponse response = buildListingResponse(ListingType.ROOM_AVAILABLE, true);
        response.setPhotos(List.of(
            ListingPhotoResponse.builder()
                .id(photoId)
                .photoUrl("https://cdn.example.com/photo-1.jpg")
                .displayOrder(0)
                .build(),
            ListingPhotoResponse.builder()
                .id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .photoUrl("https://cdn.example.com/photo-2.jpg")
                .displayOrder(1)
                .build()
        ));

        when(listingService.getListingById(eq(listingId))).thenReturn(response);
        }

    @And("I have a valid add-photo payload")
    public void iHaveAValidAddPhotoPayload() {
        addListingPhotoRequest = AddListingPhotoRequest.builder()
                .photoUrl("https://cdn.example.com/photo-1.jpg")
                .build();
    }

    @And("listing service allows removing listing photo")
    public void listingServiceAllowsRemovingListingPhoto() {
    }

    @And("listing service rejects photo operations with forbidden")
    public void listingServiceRejectsPhotoOperationsWithForbidden() {
        doThrow(new ForbiddenException("You are not authorized to update photos for this listing"))
                .when(listingService)
                .addPhotoToListing(eq(nonOwnerId), eq(listingId), any(String.class));

        doThrow(new ForbiddenException("You are not authorized to update photos for this listing"))
                .when(listingService)
                .removePhotoFromListing(eq(nonOwnerId), eq(listingId), eq(photoId));
    }

    @And("listing service allows soft delete but photo operations return not found")
    public void listingServiceAllowsSoftDeleteButPhotoOperationsReturnNotFound() {
        doThrow(new ResourceNotFoundException("Listing not found with id: " + listingId))
                .when(listingService)
                .addPhotoToListing(eq(ownerId), eq(listingId), any(String.class));
    }

    @And("listing service has 25 active listings and page size 10")
    public void listingServiceHas25ActiveListingsAndPageSize10() {
        List<ListingResponse> content = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            content.add(buildListingResponse(ListingType.ROOM_AVAILABLE, true));
        }

        when(listingService.getAllListings(eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 10), 25));
    }

    @And("listing service excludes soft-deleted listings from default query")
    public void listingServiceExcludesSoftDeletedListingsFromDefaultQuery() {
        List<ListingResponse> content = List.of(
                buildListingResponse(ListingType.ROOM_AVAILABLE, true),
                buildListingResponse(ListingType.ROOMMATE_WANTED, false)
        );

        when(listingService.getAllListings(eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 20), 2));
    }

    @And("listing service has listings in Izmir and Istanbul")
    public void listingServiceHasListingsInIzmirAndIstanbul() {
        List<ListingResponse> content = List.of(
                buildListingResponseWithCity("Izmir", ListingType.ROOMMATE_WANTED),
                buildListingResponseWithCity("Izmir", ListingType.ROOM_AVAILABLE)
        );

        when(listingService.getAllListings(eq("Izmir"), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 20), 2));
    }

    @When("I POST to {string}")
    public void iPostTo(String path) throws Exception {
        Object payload;
        if ("/api/v1/listings/room-available".equals(path)) {
            payload = listingCreateRequest;
        } else if ("/api/v1/listings/roommate-wanted".equals(path)) {
            payload = roommateWantedCreateRequest;
        } else if ("/api/v1/listings/{id}/photos".equals(path)) {
            payload = addListingPhotoRequest;
            path = "/api/v1/listings/" + listingId + "/photos";
        } else {
            throw new IllegalArgumentException("Unsupported POST path: " + path);
        }

        MockHttpServletRequestBuilder requestBuilder = post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload));

        lastResult = mockMvc.perform(requestBuilder).andReturn();
    }

    @When("I PUT to {string}")
    public void iPutTo(String path) throws Exception {
        if ("/api/v1/listings/{id}".equals(path)) {
            path = "/api/v1/listings/" + listingId;
        }

        lastResult = mockMvc.perform(
                put(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listingUpdateRequest))
        ).andReturn();
    }

    @When("I DELETE to {string}")
    public void iDeleteTo(String path) throws Exception {
        if ("/api/v1/listings/{id}".equals(path)) {
            path = "/api/v1/listings/" + listingId;
        } else if ("/api/v1/listings/{id}/photos/{photoId}".equals(path)) {
            path = "/api/v1/listings/" + listingId + "/photos/" + photoId;
        }

        lastResult = mockMvc.perform(delete(path)).andReturn();
    }

    @When("I GET {string}")
    public void iGet(String path) throws Exception {
        if ("/api/v1/listings/{id}".equals(path)) {
            path = "/api/v1/listings/" + listingId;
        }

        lastResult = mockMvc.perform(get(path)).andReturn();
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        org.junit.jupiter.api.Assertions.assertEquals(expectedStatus, lastResult.getResponse().getStatus());
    }

    @Then("the response listingType should be {string}")
    public void theResponseListingTypeShouldBe(String expectedType) throws Exception {
        String body = lastResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(body);
        org.junit.jupiter.api.Assertions.assertEquals(expectedType, root.get("listingType").asText());
    }

    @Then("the response should not contain field {string}")
    public void theResponseShouldNotContainField(String fieldName) throws Exception {
        String body = lastResult.getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertFalse(body.contains("\"" + fieldName + "\""));
    }

    @Then("all response listing types should be {string}")
    public void allResponseListingTypesShouldBe(String expectedType) throws Exception {
        String body = lastResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(body);
        for (com.fasterxml.jackson.databind.JsonNode item : root.get("content")) {
            org.junit.jupiter.api.Assertions.assertEquals(expectedType, item.get("listingType").asText());
        }
    }

    @Then("the response photoUrl should be {string}")
    public void theResponsePhotoUrlShouldBe(String expectedPhotoUrl) throws Exception {
        String body = lastResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(body);
        org.junit.jupiter.api.Assertions.assertEquals(expectedPhotoUrl, root.get("photoUrl").asText());
    }

    @Then("the response content size should be {int}")
    public void theResponseContentSizeShouldBe(int expectedSize) throws Exception {
        String body = lastResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(body);
        org.junit.jupiter.api.Assertions.assertEquals(expectedSize, root.get("content").size());
    }

    @Then("the response should indicate next page exists")
    public void theResponseShouldIndicateNextPageExists() throws Exception {
        String body = lastResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(body);
        org.junit.jupiter.api.Assertions.assertFalse(root.get("last").asBoolean());
    }

    @Then("the response photos should be ordered by displayOrder")
    public void theResponsePhotosShouldBeOrderedByDisplayOrder() throws Exception {
        String body = lastResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(body);

        int previousOrder = -1;
        for (com.fasterxml.jackson.databind.JsonNode photo : root.get("photos")) {
            int currentOrder = photo.get("displayOrder").asInt();
            org.junit.jupiter.api.Assertions.assertTrue(currentOrder >= previousOrder);
            previousOrder = currentOrder;
        }
    }

    private ListingResponse buildListingResponse(ListingType type, boolean includeRoomFields) {
        ListingResponse.ListingResponseBuilder builder = ListingResponse.builder()
                .id(listingId)
                .listingType(type)
                .title("Sample listing")
                .description("Sample listing description with sufficient length for tests")
                .city("Istanbul")
                .currency("TRY")
                .availableFrom(LocalDate.now().plusDays(15))
                .petsAllowed(true)
                .smokingAllowed(false)
                .status(ListingStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .owner(ListingOwnerResponse.builder()
                        .id(ownerId)
                        .firstName("Owner")
                        .avatarUrl("https://cdn.example.com/avatar.jpg")
                        .build());

        if (includeRoomFields) {
            builder.address("Kadikoy")
                    .postalCode("34710")
                    .rentAmount(new BigDecimal("15000"))
                    .furnished(true)
                    .utilitiesIncluded(true)
                    .photos(List.of(ListingPhotoResponse.builder()
                            .id(photoId)
                            .photoUrl("https://cdn.example.com/photo-1.jpg")
                            .displayOrder(0)
                            .build()));
        }

        return builder.build();
    }

    private ListingResponse buildListingResponseWithCity(String city, ListingType type) {
        ListingResponse response = buildListingResponse(type, type == ListingType.ROOM_AVAILABLE);
        response.setCity(city);
        return response;
    }

    private User buildUser(UUID userId, String firstName) {
        User user = User.builder()
                .email(firstName.toLowerCase() + "@mail.com")
                .passwordHash("hash")
                .firstName(firstName)
                .lastName("Test")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        user.setId(userId);
        return user;
    }

    private class AuthenticationPrincipalResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && UserDetailsImpl.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            return currentUserDetails;
        }
    }
}
