package com.surfnow.infrastructure.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3PhotoStorage implements PhotoStorage {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.cloudfront.domain:}")
    private String cloudfrontDomain;

    @Override
    public String upload(MultipartFile file, String key) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            log.info("Foto enviada para S3: {}", key);
            return buildUrl(key);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload da foto", e);
        }
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
        log.info("Foto removida do S3: {}", key);
    }

    private String buildUrl(String key) {
        if (cloudfrontDomain != null && !cloudfrontDomain.isBlank()) {
            return "https://%s/%s".formatted(cloudfrontDomain, key);
        }
        return "https://%s.s3.amazonaws.com/%s".formatted(bucket, key);
    }
}
