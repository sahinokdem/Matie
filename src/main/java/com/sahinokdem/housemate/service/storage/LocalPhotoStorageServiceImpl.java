package com.sahinokdem.housemate.service.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalPhotoStorageServiceImpl implements PhotoStorageService {

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    @Override
    public String uploadPhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file must not be empty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String generatedFileName = UUID.randomUUID() + extension;

        try {
            Files.createDirectories(UPLOAD_DIR);
            Path targetPath = UPLOAD_DIR.resolve(generatedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + generatedFileName;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store uploaded file", e);
        }
    }

    @Override
    public void deletePhoto(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        String fileName = extractFileName(fileUrl);
        if (fileName.isBlank()) {
            return;
        }

        try {
            Path targetPath = UPLOAD_DIR.resolve(fileName).normalize();
            if (targetPath.startsWith(UPLOAD_DIR.normalize())) {
                Files.deleteIfExists(targetPath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete file: " + fileName, e);
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex);
    }

    private String extractFileName(String fileUrl) {
        String sanitized = fileUrl;
        int queryIndex = sanitized.indexOf('?');
        if (queryIndex >= 0) {
            sanitized = sanitized.substring(0, queryIndex);
        }
        int lastSlash = sanitized.lastIndexOf('/');
        if (lastSlash == -1 || lastSlash == sanitized.length() - 1) {
            return "";
        }
        return sanitized.substring(lastSlash + 1);
    }
}
