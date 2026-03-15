package com.sahinokdem.housemate.service.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for photo storage operations.
 * This allows easy switching between local storage, AWS S3, Cloudinary, etc.
 */
public interface PhotoStorageService {

    /**
     * Upload a photo and return the URL/path where it's stored.
     *
     * @param file the photo file to upload
     * @return the URL or path of the uploaded photo
     */
    String uploadPhoto(MultipartFile file);

    /**
     * Delete a photo by its URL/path.
     *
     * @param fileUrl the URL or path of the photo to delete
     */
    void deletePhoto(String fileUrl);
}
