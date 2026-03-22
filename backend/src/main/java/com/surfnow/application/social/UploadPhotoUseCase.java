package com.surfnow.application.social;

import com.surfnow.infrastructure.storage.PhotoStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadPhotoUseCase {

    private final PhotoStorage photoStorage;

    public String execute(MultipartFile file, UUID userId) {
        validate(file);
        String key = "photos/%s/%s".formatted(userId, UUID.randomUUID());
        return photoStorage.upload(file, key);
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Arquivo deve ser uma imagem");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Imagem deve ter no máximo 10MB");
        }
    }
}
