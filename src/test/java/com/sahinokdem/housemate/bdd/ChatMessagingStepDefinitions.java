package com.sahinokdem.housemate.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sahinokdem.housemate.controller.ConversationController;
import com.sahinokdem.housemate.domain.application.Application;
import com.sahinokdem.housemate.domain.application.ApplicationStatus;
import com.sahinokdem.housemate.domain.chat.MessageStatus;
import com.sahinokdem.housemate.domain.listing.Listing;
import com.sahinokdem.housemate.domain.listing.ListingStatus;
import com.sahinokdem.housemate.domain.listing.ListingType;
import com.sahinokdem.housemate.domain.university.University;
import com.sahinokdem.housemate.domain.user.User;
import com.sahinokdem.housemate.domain.user.UserRole;
import com.sahinokdem.housemate.domain.user.UserStatus;
import com.sahinokdem.housemate.dto.application.ApplicationResponse;
import com.sahinokdem.housemate.dto.application.ApplicationStatusUpdateRequest;
import com.sahinokdem.housemate.dto.chat.ConversationResponse;
import com.sahinokdem.housemate.dto.chat.MessageRequest;
import com.sahinokdem.housemate.dto.chat.MessageResponse;
import com.sahinokdem.housemate.exception.ForbiddenException;
import com.sahinokdem.housemate.exception.GlobalExceptionHandler;
import com.sahinokdem.housemate.repository.ApplicationRepository;
import com.sahinokdem.housemate.repository.ListingRepository;
import com.sahinokdem.housemate.repository.UserRepository;
import com.sahinokdem.housemate.security.UserDetailsImpl;
import com.sahinokdem.housemate.service.ApplicationService;
import com.sahinokdem.housemate.service.ConversationService;
import com.sahinokdem.housemate.service.MessageService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class ChatMessagingStepDefinitions {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    private ApplicationRepository applicationRepository;
    private UserRepository userRepository;
    private ListingRepository listingRepository;
    private ConversationService conversationService;
    private MessageService messageService;
    private ApplicationService applicationService;

    private MockMvc mockMvc;
    private MvcResult lastResult;

    private final Map<String, User> usersByLabel = new HashMap<>();
    private UserDetailsImpl currentUserDetails;

    private UUID listingId;
    private UUID applicationId;
    private UUID conversationId;

    private Listing listing;
    private Application pendingApplication;
    private ApplicationResponse applicationResponse;
    private String postedMessageContent;

    @Before
    public void setUp() {
        applicationRepository = mock(ApplicationRepository.class);
        userRepository = mock(UserRepository.class);
        listingRepository = mock(ListingRepository.class);
        conversationService = mock(ConversationService.class);
        messageService = mock(MessageService.class);

        applicationService = new ApplicationService(applicationRepository, userRepository, listingRepository, conversationService);

        ConversationController conversationController = new ConversationController(conversationService, messageService);
        mockMvc = MockMvcBuilders.standaloneSetup(conversationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new ChatAuthPrincipalResolver(), new PageableHandlerMethodArgumentResolver())
                .build();

        setupDefaultUsers();
        listingId = UUID.fromString("10000000-0000-0000-0000-000000000001");
        applicationId = UUID.fromString("20000000-0000-0000-0000-000000000001");
        conversationId = UUID.fromString("30000000-0000-0000-0000-000000000001");

        listing = Listing.builder()
                .owner(usersByLabel.get("User A"))
                .listingType(ListingType.ROOM_AVAILABLE)
                .title("Nice room close to campus")
                .description("Large room in a shared apartment close to university.")
                .status(ListingStatus.ACTIVE)
                .build();
        listing.setId(listingId);

        pendingApplication = Application.builder()
                .listing(listing)
                .applicant(usersByLabel.get("User B"))
                .status(ApplicationStatus.PENDING)
                .message("Interested in your room")
                .build();
        pendingApplication.setId(applicationId);

        when(applicationRepository.findById(eq(applicationId))).thenReturn(Optional.of(pendingApplication));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        currentUserDetails = new UserDetailsImpl(usersByLabel.get("User A"));
        lastResult = null;
        applicationResponse = null;
        postedMessageContent = null;
    }

    @Given("chat API test context is initialized")
    public void chatApiTestContextIsInitialized() {
    }

    @And("a listing exists in the system")
    public void aListingExistsInTheSystem() {
        assertNotNull(listing);
        assertEquals(listingId, listing.getId());
    }

    @Given("a pending application exists for the listing")
    public void aPendingApplicationExistsForTheListing() {
        assertEquals(ApplicationStatus.PENDING, pendingApplication.getStatus());
    }

    @And("I am authenticated as the listing owner")
    public void iAmAuthenticatedAsTheListingOwner() {
        currentUserDetails = new UserDetailsImpl(usersByLabel.get("User A"));
    }

    @When("I update the application status to {string}")
    public void iUpdateTheApplicationStatusTo(String status) {
        ApplicationStatus target = ApplicationStatus.valueOf(status);
        ApplicationStatusUpdateRequest request = ApplicationStatusUpdateRequest.builder().status(target).build();

        applicationResponse = applicationService.updateApplicationStatus(currentUserDetails.getUser().getId(), applicationId, request);
    }

    @Then("the application status should be {string}")
    public void theApplicationStatusShouldBe(String expectedStatus) {
        assertNotNull(applicationResponse);
        assertEquals(ApplicationStatus.valueOf(expectedStatus), applicationResponse.getStatus());
    }

    @And("a new conversation should be created between the owner and the applicant")
    public void aNewConversationShouldBeCreatedBetweenTheOwnerAndTheApplicant() {
        verify(conversationService).createConversation(
                eq(usersByLabel.get("User A")),
                eq(usersByLabel.get("User B")),
                eq(listing)
        );
    }

    @And("the conversation should be linked to the listing")
    public void theConversationShouldBeLinkedToTheListing() {
        ArgumentCaptor<Listing> listingCaptor = ArgumentCaptor.forClass(Listing.class);
        verify(conversationService).createConversation(any(User.class), any(User.class), listingCaptor.capture());
        assertEquals(listingId, listingCaptor.getValue().getId());
    }

    @Given("a conversation exists between {string} and {string}")
    public void aConversationExistsBetweenAnd(String userA, String userB) {
        User first = usersByLabel.get(userA);
        User second = usersByLabel.get(userB);
        assertNotNull(first);
        assertNotNull(second);

        MessageResponse messageResponse = MessageResponse.builder()
                .id(UUID.fromString("40000000-0000-0000-0000-000000000001"))
                .conversationId(conversationId)
                .senderId(first.getId())
                .senderFirstName(first.getFirstName())
                .content("Hello, is the room still available?")
                .status(MessageStatus.SENT)
                .sentAt(Instant.now())
                .build();

        when(messageService.sendMessage(eq(first.getId()), eq(conversationId), any(MessageRequest.class)))
                .thenReturn(messageResponse);

        when(messageService.sendMessage(eq(usersByLabel.get("User C").getId()), eq(conversationId), any(MessageRequest.class)))
                .thenThrow(new ForbiddenException("not a participant"));

        Page<MessageResponse> page = new PageImpl<>(List.of(messageResponse), PageRequest.of(0, 20), 1);
        when(messageService.getMessages(eq(first.getId()), eq(conversationId), any(Pageable.class))).thenReturn(page);
        when(messageService.getMessages(eq(second.getId()), eq(conversationId), any(Pageable.class))).thenReturn(page);
        when(messageService.getMessages(eq(usersByLabel.get("User C").getId()), eq(conversationId), any(Pageable.class)))
                .thenThrow(new ForbiddenException("not a participant"));
    }

    @And("I am authenticated as {string}")
    public void iAmAuthenticatedAs(String userLabel) {
        User user = usersByLabel.get(userLabel);
        assertNotNull(user);
        currentUserDetails = new UserDetailsImpl(user);
    }

    @Given("I am authenticated as {string} \\(Unauthorized\\)")
    public void iAmAuthenticatedAsUnauthorized(String userLabel) {
        iAmAuthenticatedAs(userLabel);
    }

    @When("I POST a message {string} to the conversation")
    public void iPostAMessageToTheConversation(String message) throws Exception {
        postedMessageContent = message;
        MessageRequest request = MessageRequest.builder().content(message).build();

        lastResult = mockMvc.perform(
                post("/api/v1/conversations/" + conversationId + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andReturn();
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertNotNull(lastResult);
        assertEquals(expectedStatus, lastResult.getResponse().getStatus());
    }

    @And("the message content should match {string}")
    public void theMessageContentShouldMatch(String expected) throws Exception {
        JsonNode root = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertEquals(expected, root.get("content").asText());
        assertEquals(postedMessageContent, root.get("content").asText());
    }

    @And("the message sender should be {string}")
    public void theMessageSenderShouldBe(String expectedUserLabel) throws Exception {
        JsonNode root = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertEquals(usersByLabel.get(expectedUserLabel).getFirstName(), root.get("senderFirstName").asText());
    }

    @When("I attempt to POST a message to that conversation")
    public void iAttemptToPOSTAMessageToThatConversation() throws Exception {
        MessageRequest request = MessageRequest.builder().content("Should fail").build();

        lastResult = mockMvc.perform(
                post("/api/v1/conversations/" + conversationId + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andReturn();
    }

    @And("the error message should contain {string}")
    public void theErrorMessageShouldContain(String text) throws Exception {
        JsonNode root = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertTrue(root.get("message").asText().contains(text));
    }

    @When("I attempt to GET messages for that conversation")
    public void iAttemptToGETMessagesForThatConversation() throws Exception {
        lastResult = mockMvc.perform(
                get("/api/v1/conversations/" + conversationId + "/messages")
        ).andReturn();
    }

    @Given("I am authenticated as a user with 3 active conversations")
    public void iAmAuthenticatedAsAUserWith3ActiveConversations() {
        User user = usersByLabel.get("User A");
        currentUserDetails = new UserDetailsImpl(user);

        List<ConversationResponse> conversations = List.of(
                conversation("Room A", "Last message A"),
                conversation("Room B", "Last message B"),
                conversation("Room C", "Last message C")
        );
        when(conversationService.getConversationsForUser(eq(user.getId()))).thenReturn(conversations);
    }

    @When("I GET {string}")
    public void iGET(String path) throws Exception {
        lastResult = mockMvc.perform(get(path)).andReturn();
    }

    @And("the list should contain {int} conversations")
    public void theListShouldContainConversations(int expected) throws Exception {
        JsonNode root = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertEquals(expected, root.size());
    }

    @And("each conversation should show a preview of the last message")
    public void eachConversationShouldShowAPreviewOfTheLastMessage() throws Exception {
        JsonNode root = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        for (JsonNode item : root) {
            assertTrue(item.hasNonNull("lastMessagePreview"));
            assertFalse(item.get("lastMessagePreview").asText().isBlank());
        }
    }

    @Given("a conversation exists with 25 messages")
    public void aConversationExistsWith25Messages() {
        User participant = usersByLabel.get("User A");

        List<MessageResponse> content = java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> MessageResponse.builder()
                        .id(UUID.randomUUID())
                        .conversationId(conversationId)
                        .senderId(participant.getId())
                        .senderFirstName(participant.getFirstName())
                        .content("msg-" + i)
                        .status(MessageStatus.SENT)
                        .sentAt(Instant.now())
                        .build())
                .toList();

        Page<MessageResponse> page = new PageImpl<>(content, PageRequest.of(0, 10), 25);
        when(messageService.getMessages(eq(participant.getId()), eq(conversationId), any(Pageable.class))).thenReturn(page);
    }

    @And("I am authenticated as a participant")
    public void iAmAuthenticatedAsAParticipant() {
        currentUserDetails = new UserDetailsImpl(usersByLabel.get("User A"));
    }

    @When("I GET messages for the conversation with page size {int}")
    public void iGETMessagesForTheConversationWithPageSize(int pageSize) throws Exception {
        lastResult = mockMvc.perform(
                get("/api/v1/conversations/" + conversationId + "/messages?size=" + pageSize + "&page=0")
        ).andReturn();
    }

    @And("the message list size should be {int}")
    public void theMessageListSizeShouldBe(int expectedSize) throws Exception {
        JsonNode root = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertEquals(expectedSize, root.get("content").size());
    }

    @And("the response should indicate that a next page exists")
    public void theResponseShouldIndicateThatANextPageExists() throws Exception {
        JsonNode root = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertFalse(root.get("last").asBoolean());
    }

    private ConversationResponse conversation(String listingTitle, String lastMessagePreview) {
        return ConversationResponse.builder()
                .id(UUID.randomUUID())
                .listingId(listingId)
                .listingTitle(listingTitle)
                .otherUserId(usersByLabel.get("User B").getId())
                .otherUserFirstName(usersByLabel.get("User B").getFirstName())
                .otherUserAvatarUrl("https://cdn.example.com/avatar.jpg")
                .lastMessagePreview(lastMessagePreview)
                .lastMessageAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private void setupDefaultUsers() {
        University university = University.builder()
                .name("İzmir Yüksek Teknoloji Enstitüsü")
                .shortName("IYTE")
                .domain("iyte.edu.tr")
                .active(true)
                .build();
        university.setId(UUID.fromString("90000000-0000-0000-0000-000000000001"));

        usersByLabel.put("User A", buildUser(UUID.fromString("a0000000-0000-0000-0000-000000000001"), "UserA", university));
        usersByLabel.put("User B", buildUser(UUID.fromString("b0000000-0000-0000-0000-000000000001"), "UserB", university));
        usersByLabel.put("User C", buildUser(UUID.fromString("c0000000-0000-0000-0000-000000000001"), "UserC", university));
    }

    private User buildUser(UUID id, String firstName, University university) {
        User user = User.builder()
                .university(university)
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

    private class ChatAuthPrincipalResolver implements HandlerMethodArgumentResolver {
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
