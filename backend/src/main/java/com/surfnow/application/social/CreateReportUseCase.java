package com.surfnow.application.social;

import com.surfnow.domain.entity.SurfReport;
import com.surfnow.domain.repository.BeachRepository;
import com.surfnow.domain.repository.SurfReportRepository;
import com.surfnow.domain.score.CrowdLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateReportUseCase {

    private final SurfReportRepository reportRepository;
    private final BeachRepository beachRepository;

    public record Input(
            UUID beachId,
            UUID userId,
            CrowdLevel crowdLevel,
            String photoUrl,
            String description
    ) {}

    public SurfReport execute(Input input) {
        beachRepository.findById(input.beachId())
                .orElseThrow(() -> new RuntimeException("Praia não encontrada: " + input.beachId()));

        SurfReport report = SurfReport.builder()
                .beachId(input.beachId())
                .userId(input.userId())
                .crowdLevel(input.crowdLevel())
                .photoUrl(input.photoUrl())
                .description(input.description())
                .build();

        return reportRepository.save(report);
    }
}
