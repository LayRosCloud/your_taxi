package com.leafall.yourtaxi.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    private final String rootLocation;

    public FileUploadService() {
        this.rootLocation = "./uploads";
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(rootLocation));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public Resource loadAsResource(String path, String filename) {
        try {
            Path file = Paths.get(rootLocation, path).resolve(filename).normalize().toAbsolutePath();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException | FileNotFoundException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    public String store(MultipartFile file, String path) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID() + extension;

        try {
            Path dir = Paths.get(rootLocation, path);
            Files.createDirectories(dir);
            Path destinationFile = dir.resolve(uniqueFilename).normalize().toAbsolutePath();

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public void delete(String path, String filename) {
        try {
            Path dir = Paths.get(rootLocation, path);
            Path file = dir.resolve(filename).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + filename, e);
        }
    }

    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
