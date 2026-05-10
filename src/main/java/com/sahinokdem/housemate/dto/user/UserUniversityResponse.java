package com.sahinokdem.housemate.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUniversityResponse {

    private UUID id;
    private String name;
    private String shortName;
    private String domain;
}
