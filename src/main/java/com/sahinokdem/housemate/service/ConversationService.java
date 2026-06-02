package com.sahinokdem.housemate.service;

import com.sahinokdem.housemate.domain.chat.Conversation;
import com.sahinokdem.housemate.domain.chat.Message;
import com.sahinokdem.housemate.domain.application.Application;
import com.sahinokdem.housemate.domain.application.ApplicationStatus;
import com.sahinokdem.housemate.domain.listing.Listing;
import com.sahinokdem.housemate.domain.user.User;
import com.sahinokdem.housemate.dto.chat.ConversationResponse;
import com.sahinokdem.housemate.exception.BadRequestException;
import com.sahinokdem.housemate.exception.ForbiddenException;
import com.sahinokdem.housemate.exception.ResourceNotFoundException;
import com.sahinokdem.housemate.repository.ApplicationRepository;
import com.sahinokdem.housemate.repository.ConversationRepository;
import com.sahinokdem.housemate.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public Conversation createConversation(User user1, User user2, Listing listing) {
        return conversationRepository
                .findByListingAndUserPair(listing.getId(), user1.getId(), user2.getId())
                .orElseGet(() -> createNewConversation(user1, user2, listing));
    }

    private Conversation createNewConversation(User user1, User user2, Listing listing) {
        if (!listing.getOwner().getId().equals(user1.getId()) && !listing.getOwner().getId().equals(user2.getId())) {
            throw new BadRequestException("Conversation participants must include listing owner");
        }

        User applicant = listing.getOwner().getId().equals(user1.getId()) ? user2 : user1;

        Application acceptedApplication = applicationRepository
                .findByListingIdAndApplicantIdAndStatus(listing.getId(), applicant.getId(), ApplicationStatus.ACCEPTED)
                .orElseThrow(() -> new ResourceNotFoundException("Accepted application not found for listing and applicant"));

        Conversation conversation = Conversation.builder()
                .application(acceptedApplication)
                .build();
        
        // 1. Önce sohbet odasını kaydediyoruz
        Conversation savedConversation = conversationRepository.save(conversation);

        // 2. EKSİK OLAN MANTIK: Başvurudaki mesajı alıp ilk mesaj olarak odaya fırlatıyoruz!
        if (acceptedApplication.getMessage() != null && !acceptedApplication.getMessage().isBlank()) {
            Message initialMessage = Message.builder()
                    .conversation(savedConversation)
                    .sender(applicant) // Mesajı başvuran (Veli) atıyor
                    .content(acceptedApplication.getMessage())
                    .status(com.sahinokdem.housemate.domain.chat.MessageStatus.SENT) 
                    .sentAt(java.time.Instant.now())
                    .build();
            messageRepository.save(initialMessage); // Mesajı veritabanına yaz
        }

        return savedConversation;
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversationsForUser(UUID userId) {
        return conversationRepository
                .findAllByApplicationListingOwnerIdOrApplicationApplicantIdOrderByUpdatedAtDesc(userId, userId)
                .stream()
                .map(conversation -> mapToResponse(conversation, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public Conversation getConversationForUser(UUID userId, UUID conversationId) {
        Conversation conversation = conversationRepository.findWithParticipantsById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        boolean participant = conversation.getApplication().getApplicant().getId().equals(userId)
                || conversation.getApplication().getListing().getOwner().getId().equals(userId);
        if (!participant) {
            throw new ForbiddenException("You are not authorized to access this conversation");
        }
        return conversation;
    }

    private ConversationResponse mapToResponse(Conversation conversation, UUID currentUserId) {
        User listingOwner = conversation.getApplication().getListing().getOwner();
        User applicant = conversation.getApplication().getApplicant();

        User otherUser = listingOwner.getId().equals(currentUserId) ? applicant : listingOwner;

        Message lastMessage = messageRepository.findFirstByConversationIdOrderBySentAtDesc(conversation.getId())
                .orElse(null);

        String lastMessagePreview = null;
        java.time.Instant lastMessageAt = null;
        if (lastMessage != null) {
            lastMessagePreview = buildPreview(lastMessage.getContent());
            lastMessageAt = lastMessage.getSentAt();
        }

        return ConversationResponse.builder()
                .id(conversation.getId())
                .listingId(conversation.getApplication().getListing().getId())
                .listingTitle(conversation.getApplication().getListing().getTitle())
                .otherUserId(otherUser.getId())
                .otherUserFirstName(otherUser.getFirstName())
                .otherUserAvatarUrl(otherUser.getAvatarUrl())
                .lastMessagePreview(lastMessagePreview)
                .lastMessageAt(lastMessageAt)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private String buildPreview(String content) {
        if (content == null) {
            return null;
        }
        String normalized = content.trim();
        if (normalized.length() <= 120) {
            return normalized;
        }
        return normalized.substring(0, 117) + "...";
    }
}
