package com.sahinokdem.housemate.dto.chat;

import com.sahinokdem.housemate.domain.chat.MessageStatus;
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
public class MessageResponse {

    private UUID id;
    private UUID conversationId;
    private UUID senderId;
    private String senderFirstName;
    private String content;
    private MessageStatus status;
    private Instant sentAt;
}
