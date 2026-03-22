package com.surfnow.application.beach;

import com.surfnow.domain.entity.Beach;
import com.surfnow.domain.repository.BeachRepository;
import com.surfnow.domain.score.SurfConditions;
import com.surfnow.domain.score.SurfScoreCalculator;
import com.surfnow.infrastructure.external.SurfDataProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListBestBeachesUseCase {

    private final BeachRepository beachRepository;
    private final SurfDataProvider surfDataProvider;
    private final SurfScoreCalculator scoreCalculator;

    public List<GetBeachScoreUseCase.BeachScore> execute(String state) {
        List<Beach> beaches = state != null
                ? beachRepository.findByState(state)
                : beachRepository.findAll();

        return beaches.stream()
                .map(this::toBeachScore)
                .sorted(Comparator.comparingDouble(GetBeachScoreUseCase.BeachScore::score).reversed())
                .toList();
    }

    private GetBeachScoreUseCase.BeachScore toBeachScore(Beach beach) {
        SurfConditions conditions = surfDataProvider.getCurrentConditions(
                beach.getLatitude(), beach.getLongitude());
        double score = scoreCalculator.calculate(conditions, beach);
        return new GetBeachScoreUseCase.BeachScore(beach, conditions, score, scoreCalculator.scoreLabel(score));
    }
}
