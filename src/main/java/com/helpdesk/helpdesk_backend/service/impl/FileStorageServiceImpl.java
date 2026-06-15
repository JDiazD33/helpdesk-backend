package com.helpdesk.helpdesk_backend.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.helpdesk.helpdesk_backend.service.FileStorageService;

import jakarta.annotation.PostConstruct;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.dir:uploads/cierres}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de subidas: " + uploadDir, e);
        }
    }

    @Override
    public String guardarImagen(MultipartFile file, Long ticketId) {
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniqueName = datePart + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        Path targetPath = Paths.get(uploadDir, uniqueName);
        try {
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }
        return "/uploads/cierres/" + uniqueName;
    }
}
