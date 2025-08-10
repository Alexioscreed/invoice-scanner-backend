package com.invoice_scanner_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // Clean file name and generate unique name
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFileName);
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // Check for invalid characters
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Invalid file name: " + originalFileName);
            }

            // Copy file to upload location
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    public byte[] loadFileAsBytes(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
            return Files.readAllBytes(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("File not found: " + fileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file: " + fileName, ex);
        }
    }

    public boolean fileExists(String fileName) {
        Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
        return Files.exists(filePath);
    }

    public String getFilePath(String fileName) {
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName).toString();
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new RuntimeException("File name is invalid");
        }

        // Check file size (limit to 10MB)
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds maximum limit of 10MB");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new RuntimeException("File type not allowed. Only PDF, PNG, JPG, and JPEG files are permitted");
        }
    }

    private boolean isAllowedContentType(String contentType) {
        return contentType.equals("application/pdf") ||
               contentType.equals("image/png") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/jpeg");
    }
}
