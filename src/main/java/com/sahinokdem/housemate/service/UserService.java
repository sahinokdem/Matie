package com.sahinokdem.housemate.service;

import com.sahinokdem.housemate.domain.user.User;
import com.sahinokdem.housemate.domain.user.UserStatus;
import com.sahinokdem.housemate.dto.user.UserDeleteRequest;
import com.sahinokdem.housemate.dto.user.UserProfileResponse;
import com.sahinokdem.housemate.dto.user.UserProfileUpdateRequest;
import com.sahinokdem.housemate.exception.ResourceNotFoundException;
import com.sahinokdem.housemate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return mapToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update fields if provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        User updatedUser = userRepository.save(user);
        return mapToProfileResponse(updatedUser);
    }

    @Transactional
    public void softDeleteUser(UUID userId, UserDeleteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Perform soft delete - DO NOT call repository.delete()
        user.setStatus(UserStatus.DELETED);
        user.setDeletedAt(Instant.now());
        user.setDeletedBy(userId);
        user.setDeletionReason(request.getDeletionReason());

        userRepository.save(user);
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
