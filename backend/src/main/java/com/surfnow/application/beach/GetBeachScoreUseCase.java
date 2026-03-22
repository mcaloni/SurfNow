package com.surfnow.application.beach;

import com.surfnow.domain.entity.Beach;
import com.surfnow.domain.repository.BeachRepository;
import com.surfnow.domain.score.SurfConditions;
import com.surfnow.domain.score.SurfScoreCalculator;
import com.surfnow.infrastructure.external.SurfDataProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetBeachScoreUseCase {

    private final BeachRepository beachRepository;
    private final SurfDataProvider surfDataProvider;
    private final SurfScoreCalculator scoreCalculator;

    public BeachScore execute(UUID beachId) {
        Beach beach = beachRepository.findById(beachId)
                .orElseThrow(() -> new BeachNotFoundException(beachId));

        SurfConditions conditions = surfDataProvider.getCurrentConditions(
                beach.getLatitude(), beach.getLongitude());

        double score = scoreCalculator.calculate(conditions, beach);
        String label = scoreCalculator.scoreLabel(score);

        return new BeachScore(beach, conditions, score, label);
    }

    public record BeachScore(
            Beach beach,
            SurfConditions conditions,
            double score,
            String label
    ) {}

    public static class BeachNotFoundException extends RuntimeException {
        public BeachNotFoundException(UUID id) {
            super("Praia não encontrada: " + id);
        }
    }
}
