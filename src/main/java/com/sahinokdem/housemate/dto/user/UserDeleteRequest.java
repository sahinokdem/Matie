package com.sahinokdem.housemate.dto.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeleteRequest {

    @Size(max = 500, message = "Deletion reason must not exceed 500 characters")
    private String deletionReason;
}
