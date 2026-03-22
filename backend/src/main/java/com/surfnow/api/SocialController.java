package com.surfnow.api;

import com.surfnow.api.dto.PhotoUploadResponse;
import com.surfnow.api.dto.SurfReportRequest;
import com.surfnow.application.social.CreateReportUseCase;
import com.surfnow.application.social.UploadPhotoUseCase;
import com.surfnow.domain.entity.SurfReport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SocialController {

    private final UploadPhotoUseCase uploadPhotoUseCase;
    private final CreateReportUseCase createReportUseCase;

    /**
     * Faz upload de uma foto para o S3.
     * TODO: substituir X-User-Id por JWT quando auth for implementado.
     */
    @PostMapping("/photos")
    public ResponseEntity<PhotoUploadResponse> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") UUID userId) {
        String photoUrl = uploadPhotoUseCase.execute(file, userId);
        return ResponseEntity.ok(new PhotoUploadResponse(photoUrl));
    }

    /**
     * Cria um relatório de condições (crowdsource) para uma praia.
     */
    @PostMapping("/reports")
    public ResponseEntity<SurfReport> createReport(
            @Valid @RequestBody SurfReportRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        var input = new CreateReportUseCase.Input(
                request.beachId(),
                userId,
                request.crowdLevel(),
                request.photoUrl(),
                request.description()
        );
        return ResponseEntity.ok(createReportUseCase.execute(input));
    }
}
