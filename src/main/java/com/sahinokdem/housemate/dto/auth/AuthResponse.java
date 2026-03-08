package com.sahinokdem.housemate.dto.auth;

import com.sahinokdem.housemate.domain.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private UUID userId;
    private String email;
    private UserRole role;
    private String firstName;
    private String lastName;
}
