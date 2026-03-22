package com.surfnow.domain.score;

import java.time.LocalDateTime;

/**
 * Value object com as condições de surf em um determinado momento.
 * Obtido via API externa (Stormglass ou Open-Meteo).
 */
public record SurfConditions(
        double waveHeight,      // metros
        double wavePeriod,      // segundos
        double windSpeed,       // km/h
        double windDirection,   // graus (0–360, onde 0/360 = Norte)
        double swellDirection,  // graus (0–360)
        double swellHeight,     // metros
        double swellPeriod,     // segundos
        LocalDateTime timestamp
) {}
