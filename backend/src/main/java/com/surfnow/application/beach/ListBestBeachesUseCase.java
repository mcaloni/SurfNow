package com.surfnow.application.beach;

import com.surfnow.domain.entity.Beach;
import com.surfnow.domain.repository.BeachRepository;
import com.surfnow.domain.score.SurfConditions;
import com.surfnow.domain.score.SurfScoreCalculator;
import com.surfnow.infrastructure.external.SurfDataProvider;
import com.surfnow.infrastructure.external.windguru.WindguruForecastPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ListBestBeachesUseCase {

    private final BeachRepository beachRepository;
    private final SurfDataProvider surfDataProvider;
    private final SurfScoreCalculator scoreCalculator;
    private final WindguruForecastPort windguruForecastPort;

    public List<GetBeachScoreUseCase.BeachScore> execute(String state, Double userLat, Double userLng) {
        List<Beach> beaches = state != null
                ? beachRepository.findByState(state)
                : beachRepository.findAll();

        var scored = beaches.parallelStream().map(this::toBeachScore).toList();

        if (userLat != null && userLng != null) {
            return scored.stream()
                    .sorted(Comparator.comparingDouble(b -> distanceKm(userLat, userLng,
                            b.beach().getLatitude(), b.beach().getLongitude())))
                    .toList();
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(GetBeachScoreUseCase.BeachScore::score).reversed())
                .toList();
    }

    private GetBeachScoreUseCase.BeachScore toBeachScore(Beach beach) {
        Optional<SurfConditions> windguru = beach.getWindguruSpotId() != null
                ? windguruForecastPort.fetch(beach.getWindguruSpotId())
                : Optional.empty();

        // TESTE: usar apenas Windguru quando disponível.
        // Open-Meteo é chamado só para complementar waveHeight/wavePeriod (não fornecidos pelo Windguru).
        SurfConditions conditions;
        if (windguru.isPresent()) {
            SurfConditions wg = windguru.get();
            SurfConditions om = surfDataProvider.getCurrentConditions(beach.getLatitude(), beach.getLongitude());
            conditions = new SurfConditions(
                    om.waveHeight(),   // Open-Meteo: único que fornece
                    om.wavePeriod(),   // Open-Meteo: único que fornece
                    wg.windSpeed(),
                    wg.windDirection(),
                    wg.swellDirection(),
                    wg.swellHeight(),
                    wg.swellPeriod(),
                    wg.timestamp()
            );
        } else {
            conditions = surfDataProvider.getCurrentConditions(beach.getLatitude(), beach.getLongitude());
        }

        double score = scoreCalculator.calculate(conditions, beach);
        return new GetBeachScoreUseCase.BeachScore(beach, conditions, score, scoreCalculator.scoreLabel(score));
    }

    private double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
