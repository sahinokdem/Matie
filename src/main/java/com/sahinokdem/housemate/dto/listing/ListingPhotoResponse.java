package com.sahinokdem.housemate.dto.listing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingPhotoResponse {

    private UUID id;
    private String photoUrl;
    private Integer displayOrder;
}
