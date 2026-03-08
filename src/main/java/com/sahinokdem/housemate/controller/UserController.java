package com.sahinokdem.housemate.controller;

import com.sahinokdem.housemate.dto.user.UserDeleteRequest;
import com.sahinokdem.housemate.dto.user.UserProfileResponse;
import com.sahinokdem.housemate.dto.user.UserProfileUpdateRequest;
import com.sahinokdem.housemate.security.UserDetailsImpl;
import com.sahinokdem.housemate.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management APIs for viewing, updating, and deleting the current user's profile")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Get current user profile",
            description = "Returns the profile information of the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        UserProfileResponse response = userService.getCurrentUserProfile(userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(
            summary = "Update current user profile",
            description = "Updates the profile information of the currently authenticated user. Only provided fields will be updated."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        UserProfileResponse response = userService.updateProfile(userDetails.getUser().getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Operation(
            summary = "Delete current user account",
            description = "Soft deletes the currently authenticated user's account. The account is marked as deleted but data is retained."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Account deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid deletion reason",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteCurrentUserAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody(required = false) UserDeleteRequest request
    ) {
        UserDeleteRequest deleteRequest = request != null ? request : new UserDeleteRequest();
        userService.softDeleteUser(userDetails.getUser().getId(), deleteRequest);
        return ResponseEntity.noContent().build();
    }
}
