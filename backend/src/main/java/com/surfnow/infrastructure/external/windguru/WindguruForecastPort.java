package com.surfnow.infrastructure.external.windguru;

import com.surfnow.domain.score.SurfConditions;

import java.util.Optional;

/**
 * Porta (interface) para buscar previsão de surf no Windguru por spot ID.
 *
 * Implementações disponíveis:
 *   - {@link WindguruProApiClient}     → API oficial Micro API (ativo quando credenciais configuradas)
 *   - {@link WindguruScrapingClient}   → Scraping HTML como fallback gratuito
 */
public interface WindguruForecastPort {

    /**
     * Busca condições previstas para o spot Windguru.
     * Retorna {@link Optional#empty()} silenciosamente em caso de falha —
     * o sistema continua com Open-Meteo como fonte principal.
     */
    Optional<SurfConditions> fetch(int spotId);
}
