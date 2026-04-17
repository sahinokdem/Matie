package com.sahinokdem.housemate.controller;

import com.sahinokdem.housemate.dto.university.UniversityRequest;
import com.sahinokdem.housemate.dto.university.UniversityResponse;
import com.sahinokdem.housemate.service.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/universities")
@RequiredArgsConstructor
@Tag(name = "Universities", description = "University APIs for dropdown and admin university management")
public class UniversityController {

    private final UniversityService universityService;

    @GetMapping
    @Operation(summary = "Get active universities", description = "Returns active universities for registration dropdown")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Universities retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UniversityResponse.class)))
    })
    public ResponseEntity<List<UniversityResponse>> getActiveUniversities() {
        return ResponseEntity.ok(universityService.getActiveUniversities());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create university", description = "Creates a new university (admin only)")
    public ResponseEntity<UniversityResponse> createUniversity(@Valid @RequestBody UniversityRequest request) {
        UniversityResponse response = universityService.createUniversity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update university", description = "Updates an existing university (admin only)")
    public ResponseEntity<UniversityResponse> updateUniversity(
            @PathVariable UUID id,
            @Valid @RequestBody UniversityRequest request
    ) {
        UniversityResponse response = universityService.updateUniversity(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete university", description = "Soft deletes (deactivates) a university (admin only)")
    public ResponseEntity<Void> deleteUniversity(@PathVariable UUID id) {
        universityService.deleteUniversity(id);
        return ResponseEntity.noContent().build();
    }
}
