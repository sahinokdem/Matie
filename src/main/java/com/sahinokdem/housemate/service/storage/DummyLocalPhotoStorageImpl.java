package com.sahinokdem.housemate.service.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Dummy implementation for local photo storage.
 * This is a placeholder until we integrate AWS S3 or Cloudinary.
 * For MVP Day 4, we accept raw photo URLs in DTOs.
 */
public class DummyLocalPhotoStorageImpl implements PhotoStorageService {

    @Override
    public String uploadPhoto(MultipartFile file) {
        throw new UnsupportedOperationException(
                "Photo upload not implemented yet. Use direct photo URLs in the DTO for now."
        );
    }

    @Override
    public void deletePhoto(String fileUrl) {
        throw new UnsupportedOperationException(
                "Photo deletion not implemented yet. This will be implemented when we add AWS S3/Cloudinary."
        );
    }
}
