package com.sahinokdem.housemate.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {

    private UUID id;
    private UUID listingId;
    private String listingTitle;

    private UUID otherUserId;
    private String otherUserFirstName;
    private String otherUserAvatarUrl;

    private String lastMessagePreview;
    private Instant lastMessageAt;

    private Instant createdAt;
    private Instant updatedAt;
}
