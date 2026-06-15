package com.helpdesk.helpdesk_backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String guardarImagen(MultipartFile file, Long ticketId);
}
