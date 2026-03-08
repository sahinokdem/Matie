package com.sahinokdem.housemate.domain.chat;

import com.sahinokdem.housemate.domain.BaseEntity;
import com.sahinokdem.housemate.domain.application.Application;
import com.sahinokdem.housemate.domain.listing.ListingPhoto;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "conversations")
@Getter(AccessLevel.NONE)
@Setter(AccessLevel.NONE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sentAt DESC")
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    // Helper methods
    public void addMessage(Message message) {
        messages.add(message);
        message.setConversation(this);
    }

    public void removeMessage(Message message) {
        messages.remove(message);
        message.setConversation(null);
    }

    // Listeyi sadece okunabilir (değiştirilemez) olarak dışarı açıyoruz
    public List<ListingPhoto> getPhotos() {
        return Collections.unmodifiableList(getPhotos());
    }
}
