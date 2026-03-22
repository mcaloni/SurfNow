package com.surfnow.api.dto;

import com.surfnow.application.beach.GetBeachScoreUseCase;
import com.surfnow.domain.entity.Beach;
import com.surfnow.domain.score.SurfConditions;

import java.time.LocalDateTime;
import java.util.UUID;

public record BeachResponse(
        UUID id,
        String name,
        String city,
        String state,
        double latitude,
        double longitude,
        double score,
        String scoreLabel,
        SurfConditionsDto conditions
) {
    public static BeachResponse from(GetBeachScoreUseCase.BeachScore beachScore) {
        Beach beach = beachScore.beach();
        SurfConditions c = beachScore.conditions();

        return new BeachResponse(
                beach.getId(),
                beach.getName(),
                beach.getCity(),
                beach.getState(),
                beach.getLatitude(),
                beach.getLongitude(),
                Math.round(beachScore.score() * 10.0) / 10.0,
                beachScore.label(),
                new SurfConditionsDto(
                        c.waveHeight(),
                        c.wavePeriod(),
                        c.windSpeed(),
                        c.windDirection(),
                        c.swellDirection(),
                        c.timestamp()
                )
        );
    }

    public record SurfConditionsDto(
            double waveHeight,
            double wavePeriod,
            double windSpeed,
            double windDirection,
            double swellDirection,
            LocalDateTime lastUpdated
    ) {}
}
