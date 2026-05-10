package com.sahinokdem.housemate.service;

import com.sahinokdem.housemate.domain.chat.Conversation;
import com.sahinokdem.housemate.domain.chat.Message;
import com.sahinokdem.housemate.domain.chat.MessageStatus;
import com.sahinokdem.housemate.domain.user.User;
import com.sahinokdem.housemate.dto.chat.MessageRequest;
import com.sahinokdem.housemate.dto.chat.MessageResponse;
import com.sahinokdem.housemate.exception.ResourceNotFoundException;
import com.sahinokdem.housemate.repository.MessageRepository;
import com.sahinokdem.housemate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationService conversationService;

    @Transactional
    public MessageResponse sendMessage(UUID senderId, UUID conversationId, MessageRequest request) {
        Conversation conversation = conversationService.getConversationForUser(senderId, conversationId);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + senderId));

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent().trim())
                .status(MessageStatus.SENT)
                .build();

        Message savedMessage = messageRepository.save(message);
        return mapToResponse(savedMessage);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(UUID userId, UUID conversationId, Pageable pageable) {
        conversationService.getConversationForUser(userId, conversationId);

        return messageRepository.findAllByConversationIdOrderBySentAtDesc(conversationId, pageable)
                .map(this::mapToResponse);
    }

    private MessageResponse mapToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderFirstName(message.getSender().getFirstName())
                .content(message.getContent())
                .status(message.getStatus())
                .sentAt(message.getSentAt())
                .build();
    }
}
