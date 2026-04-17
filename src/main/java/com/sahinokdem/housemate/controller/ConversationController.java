package com.sahinokdem.housemate.controller;

import com.sahinokdem.housemate.dto.chat.ConversationResponse;
import com.sahinokdem.housemate.dto.chat.MessageRequest;
import com.sahinokdem.housemate.dto.chat.MessageResponse;
import com.sahinokdem.housemate.security.UserDetailsImpl;
import com.sahinokdem.housemate.service.ConversationService;
import com.sahinokdem.housemate.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Messaging APIs for chat conversations and messages")
@SecurityRequirement(name = "Bearer Authentication")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    @GetMapping
    @Operation(summary = "List current user's conversations", description = "Returns all conversations the current user is part of")
    @ApiResponse(responseCode = "200", description = "Conversations retrieved successfully")
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<ConversationResponse> response = conversationService.getConversationsForUser(userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Get conversation messages", description = "Returns paginated messages for a conversation ordered by newest first")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "User is not part of conversation", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conversation not found", content = @Content)
    })
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Conversation ID", required = true)
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<MessageResponse> response = messageService.getMessages(userDetails.getUser().getId(), id, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "Send message to conversation", description = "Sends a new message to a conversation where current user is a participant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message sent successfully", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid message payload", content = @Content),
            @ApiResponse(responseCode = "403", description = "User is not part of conversation", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conversation not found", content = @Content)
    })
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Conversation ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody MessageRequest request
    ) {
        MessageResponse response = messageService.sendMessage(userDetails.getUser().getId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
