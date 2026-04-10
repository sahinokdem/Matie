package com.sahinokdem.housemate.controller;

import com.sahinokdem.housemate.dto.listing.AddListingPhotoRequest;
import com.sahinokdem.housemate.dto.listing.ListingCreateRequest;
import com.sahinokdem.housemate.dto.listing.ListingPhotoResponse;
import com.sahinokdem.housemate.dto.listing.ListingResponse;
import com.sahinokdem.housemate.dto.listing.RoommateWantedCreateRequest;
import com.sahinokdem.housemate.dto.listing.RoommateWantedUpdateRequest;
import com.sahinokdem.housemate.dto.listing.ListingUpdateRequest;
import com.sahinokdem.housemate.security.UserDetailsImpl;
import com.sahinokdem.housemate.service.ListingService;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
@Tag(name = "Listings", description = "Listing management APIs for creating, viewing, updating, and deleting room/roommate listings")
@SecurityRequirement(name = "Bearer Authentication")
public class ListingController {

    private final ListingService listingService;

    @PostMapping("/room-available")
    @Operation(
            summary = "Create ROOM_AVAILABLE listing",
            description = "Creates a room-available listing for users who are offering a room."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Listing created successfully",
                    content = @Content(schema = @Schema(implementation = ListingResponse.class))
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
            )
    })
    public ResponseEntity<ListingResponse> createRoomAvailableListing(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ListingCreateRequest request
    ) {
        ListingResponse response = listingService.createRoomAvailableListing(userDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/roommate-wanted")
    @Operation(
            summary = "Create ROOMMATE_WANTED listing",
            description = "Creates a roommate-wanted listing for users looking for a room or roommate."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Listing created successfully",
                    content = @Content(schema = @Schema(implementation = ListingResponse.class))
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
            )
    })
    public ResponseEntity<ListingResponse> createRoommateWantedListing(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody RoommateWantedCreateRequest request
    ) {
        ListingResponse response = listingService.createRoommateWantedListing(userDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get listing by ID",
            description = "Retrieves detailed information about a specific listing including owner details and photos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listing retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ListingResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Listing not found",
                    content = @Content
            )
    })
    public ResponseEntity<ListingResponse> getListingById(
            @Parameter(description = "Listing ID", required = true)
            @PathVariable UUID id
    ) {
        ListingResponse response = listingService.getListingById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "Get all listings",
            description = "Retrieves a paginated list of active listings. Optionally filter by city and type (ALL, ROOM_AVAILABLE, ROOMMATE_WANTED)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listings retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    public ResponseEntity<Page<ListingResponse>> getAllListings(
            @Parameter(description = "Filter by city (optional)")
            @RequestParam(required = false) String city,
                        @Parameter(description = "Filter by type (optional). Allowed: ALL, ROOM_AVAILABLE, ROOMMATE_WANTED. Default: ALL")
                        @RequestParam(defaultValue = "ALL") String type,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
                Page<ListingResponse> response = listingService.getAllListings(city, type, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update listing",
            description = "Updates an existing listing. Only the owner can update their listing."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listing updated successfully",
                    content = @Content(schema = @Schema(implementation = ListingResponse.class))
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
                    responseCode = "403",
                    description = "User not authorized to update this listing",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Listing not found",
                    content = @Content
            )
    })
    public ResponseEntity<ListingResponse> updateListing(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Listing ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody ListingUpdateRequest request
    ) {
        ListingResponse response = listingService.updateListing(userDetails.getUser().getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/roommate-wanted")
    @Operation(
            summary = "Update ROOMMATE_WANTED listing",
            description = "Updates only roommate-wanted fields (title, description, city, currency, availableFrom, petsAllowed, smokingAllowed)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listing updated successfully",
                    content = @Content(schema = @Schema(implementation = ListingResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or wrong listing type",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User not authorized to update this listing",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Listing not found",
                    content = @Content
            )
    })
    public ResponseEntity<ListingResponse> updateRoommateWantedListing(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Listing ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody RoommateWantedUpdateRequest request
    ) {
        ListingResponse response = listingService.updateRoommateWantedListing(userDetails.getUser().getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete listing",
            description = "Soft deletes a listing. Only the owner can delete their listing. The listing is marked as deleted but data is retained."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Listing deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User not authorized to delete this listing",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Listing not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteListing(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Listing ID", required = true)
            @PathVariable UUID id
    ) {
        listingService.softDeleteListing(userDetails.getUser().getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/photos")
    @Operation(
            summary = "Add photo to listing",
            description = "Adds a single photo URL to an existing listing. Only the owner can add photos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Photo added successfully", content = @Content(schema = @Schema(implementation = ListingPhotoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "User not authorized to update this listing", content = @Content),
            @ApiResponse(responseCode = "404", description = "Listing not found", content = @Content)
    })
    public ResponseEntity<ListingPhotoResponse> addPhotoToListing(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Listing ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody AddListingPhotoRequest request
    ) {
        ListingPhotoResponse response = listingService.addPhotoToListing(
                userDetails.getUser().getId(),
                id,
                request.getPhotoUrl()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    @Operation(
            summary = "Remove photo from listing",
            description = "Removes a single photo from an existing listing. Only the owner can remove photos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Photo removed successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "User not authorized to update this listing", content = @Content),
            @ApiResponse(responseCode = "404", description = "Listing or photo not found", content = @Content)
    })
    public ResponseEntity<Void> removePhotoFromListing(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Listing ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Photo ID", required = true)
            @PathVariable UUID photoId
    ) {
        listingService.removePhotoFromListing(userDetails.getUser().getId(), id, photoId);
        return ResponseEntity.noContent().build();
    }
}
