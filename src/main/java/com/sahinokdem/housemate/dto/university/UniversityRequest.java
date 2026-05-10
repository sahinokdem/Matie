package com.sahinokdem.housemate.dto.university;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityRequest {

    @NotBlank(message = "University name is required")
    @Size(max = 255, message = "University name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "University short name is required")
    @Size(max = 50, message = "University short name must not exceed 50 characters")
    private String shortName;

    @NotBlank(message = "University domain is required")
    @Size(max = 255, message = "University domain must not exceed 255 characters")
    private String domain;
}
