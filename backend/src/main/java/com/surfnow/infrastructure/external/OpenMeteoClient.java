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

/**
 * Fallback gratuito usando Open-Meteo Marine API.
 * Ativo automaticamente quando StormglassClient não está disponível (sem API key).
 * Sem limite de requisições para uso não comercial.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(StormglassClient.class)
public class OpenMeteoClient implements SurfDataProvider {

    @Value("${surf.open-meteo.base-url}")
    private String baseUrl;

    @Override
    public SurfConditions getCurrentConditions(double latitude, double longitude) {
        try {
            RestClient client = RestClient.create(baseUrl);

            var response = client.get()
                    .uri("?latitude={lat}&longitude={lng}" +
                                    "&hourly=wave_height,wave_period,wind_wave_height," +
                                    "swell_wave_height,swell_wave_period,swell_wave_direction" +
                                    "&wind_speed_unit=kmh&forecast_days=1",
                            latitude, longitude)
                    .retrieve()
                    .body(Map.class);

            return parse(response);
        } catch (Exception e) {
            log.error("Erro ao buscar dados do Open-Meteo lat={} lng={}", latitude, longitude, e);
            throw new StormglassClient.SurfDataUnavailableException("Open-Meteo indisponível", e);
        }
    }

    @SuppressWarnings("unchecked")
    private SurfConditions parse(Map<?, ?> response) {
        var hourly = (Map<String, List<Object>>) response.get("hourly");
        if (hourly == null) {
            throw new StormglassClient.SurfDataUnavailableException("Nenhum dado retornado pelo Open-Meteo");
        }

        return new SurfConditions(
                getFirst(hourly, "wave_height"),
                getFirst(hourly, "wave_period"),
                0,  // Open-Meteo Marine não inclui vento neste endpoint
                0,
                getFirst(hourly, "swell_wave_direction"),
                getFirst(hourly, "swell_wave_height"),
                getFirst(hourly, "swell_wave_period"),
                LocalDateTime.now()
        );
    }

    private double getFirst(Map<String, List<Object>> hourly, String key) {
        List<Object> values = hourly.get(key);
        if (values == null || values.isEmpty() || values.get(0) == null) return 0.0;
        return ((Number) values.get(0)).doubleValue();
    }
}
