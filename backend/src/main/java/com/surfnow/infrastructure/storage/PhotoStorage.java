package com.surfnow.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoStorage {
    String upload(MultipartFile file, String key);
    void delete(String key);
}
