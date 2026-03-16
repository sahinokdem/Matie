package com.sahinokdem.housemate.controller;

import com.sahinokdem.housemate.dto.application.ApplicationCreateRequest;
import com.sahinokdem.housemate.dto.application.ApplicationResponse;
import com.sahinokdem.housemate.dto.application.ApplicationStatusUpdateRequest;
import com.sahinokdem.housemate.security.UserDetailsImpl;
import com.sahinokdem.housemate.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Listing application management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @Operation(summary = "Create application", description = "Apply to an active listing as the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Application created successfully", content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid application request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Listing or user not found", content = @Content)
    })
    public ResponseEntity<ApplicationResponse> createApplication(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ApplicationCreateRequest request
    ) {
        ApplicationResponse response = applicationService.createApplication(userDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update application status", description = "Accept, reject, or cancel an application according to ownership rules")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application status updated successfully", content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Application not found", content = @Content)
    })
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Application ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request
    ) {
        ApplicationResponse response = applicationService.updateApplicationStatus(userDetails.getUser().getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/received")
    @Operation(summary = "Get received applications", description = "Returns paginated applications received for the current user's listings")
    @ApiResponse(responseCode = "200", description = "Received applications returned successfully")
    public ResponseEntity<Page<ApplicationResponse>> getReceivedApplications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(applicationService.getReceivedApplications(userDetails.getUser().getId(), pageable));
    }

    @GetMapping("/sent")
        @Operation(summary = "Get sent applications", description = "Returns paginated applications sent by the current user. Optional status filter: PENDING, ACCEPTED, REJECTED, CANCELLED, ALL")
    @ApiResponse(responseCode = "200", description = "Sent applications returned successfully")
    public ResponseEntity<Page<ApplicationResponse>> getSentApplications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
                        @Parameter(description = "Status filter (ALL, PENDING, ACCEPTED, REJECTED, CANCELLED)")
                        @RequestParam(defaultValue = "ALL") String status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
                return ResponseEntity.ok(applicationService.getSentApplications(userDetails.getUser().getId(), pageable, status));
    }
}
