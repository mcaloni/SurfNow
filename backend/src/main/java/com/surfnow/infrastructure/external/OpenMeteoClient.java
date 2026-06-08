package com.surfnow.infrastructure.external;

import com.surfnow.domain.score.SurfConditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fallback gratuito combinando Open-Meteo Marine API (ondas/swell) +
 * Open-Meteo Forecast API (vento). Ativo quando StormglassClient não está disponível.
 * Sem limite de requisições para uso não comercial.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(StormglassClient.class)
public class OpenMeteoClient implements SurfDataProvider {

    @Value("${surf.open-meteo.base-url}")
    private String marineUrl;

    @Value("${surf.open-meteo.forecast-url}")
    private String forecastUrl;

    @Override
    public SurfConditions getCurrentConditions(double latitude, double longitude) {
        try {
            var marineFuture = CompletableFuture.supplyAsync(() -> fetchMarine(latitude, longitude));
            var forecastFuture = CompletableFuture.supplyAsync(() -> fetchForecast(latitude, longitude));
            return parse(marineFuture.join(), forecastFuture.join());
        } catch (Exception e) {
            log.error("Erro ao buscar dados do Open-Meteo lat={} lng={}", latitude, longitude, e);
            throw new StormglassClient.SurfDataUnavailableException("Open-Meteo indisponível", e);
        }
    }

    private Map<?, ?> fetchMarine(double lat, double lng) {
        return RestClient.create(marineUrl).get()
                .uri("?latitude={lat}&longitude={lng}" +
                        "&hourly=wave_height,wave_period,swell_wave_height,swell_wave_period,swell_wave_direction" +
                        "&forecast_days=1",
                        lat, lng)
                .retrieve()
                .body(Map.class);
    }

    private Map<?, ?> fetchForecast(double lat, double lng) {
        return RestClient.create(forecastUrl).get()
                .uri("?latitude={lat}&longitude={lng}" +
                        "&hourly=wind_speed_10m,wind_direction_10m" +
                        "&wind_speed_unit=kmh&forecast_days=1",
                        lat, lng)
                .retrieve()
                .body(Map.class);
    }

    @SuppressWarnings("unchecked")
    private SurfConditions parse(Map<?, ?> marine, Map<?, ?> forecast) {
        var marineHourly = (Map<String, List<Object>>) marine.get("hourly");
        var forecastHourly = (Map<String, List<Object>>) forecast.get("hourly");

        if (marineHourly == null) {
            throw new StormglassClient.SurfDataUnavailableException("Nenhum dado retornado pelo Open-Meteo Marine");
        }

        return new SurfConditions(
                getFirst(marineHourly, "wave_height"),
                getFirst(marineHourly, "wave_period"),
                forecastHourly != null ? getFirst(forecastHourly, "wind_speed_10m") : 0,
                forecastHourly != null ? getFirst(forecastHourly, "wind_direction_10m") : 0,
                getFirst(marineHourly, "swell_wave_direction"),
                getFirst(marineHourly, "swell_wave_height"),
                getFirst(marineHourly, "swell_wave_period"),
                LocalDateTime.now()
        );
    }

    private double getFirst(Map<String, List<Object>> hourly, String key) {
        List<Object> values = hourly.get(key);
        if (values == null || values.isEmpty() || values.get(0) == null) return 0.0;
        return ((Number) values.get(0)).doubleValue();
    }
}
