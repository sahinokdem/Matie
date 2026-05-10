package com.sahinokdem.housemate.repository;

import com.sahinokdem.housemate.domain.chat.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findAllByConversationIdOrderBySentAtDesc(UUID conversationId, Pageable pageable);

    Optional<Message> findFirstByConversationIdOrderBySentAtDesc(UUID conversationId);
}
