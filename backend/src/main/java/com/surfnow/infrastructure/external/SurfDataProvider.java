package com.surfnow.infrastructure.external;

import com.surfnow.domain.score.SurfConditions;

/**
 * Port (interface) para buscar condições de surf de APIs externas.
 * Implementações: StormglassClient (produção), OpenMeteoClient (fallback gratuito).
 */
public interface SurfDataProvider {
    SurfConditions getCurrentConditions(double latitude, double longitude);
}
