package com.surfnow.infrastructure.external;

import com.surfnow.domain.score.SurfConditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Busca condições de surf via Stormglass API.
 * Ativo apenas quando surf.stormglass.api-key está configurado.
 * Plano gratuito: 10 req/dia. Plano pago: €19/mês (500 req/dia).
 */
@Slf4j
@Component
@ConditionalOnExpression("'${surf.stormglass.api-key:}'.length() > 0")
public class StormglassClient implements SurfDataProvider {

    private static final String PARAMS =
            "waveHeight,wavePeriod,windSpeed,windDirection,swellDirection,swellHeight,swellPeriod";

    @Value("${surf.stormglass.api-key}")
    private String apiKey;

    @Value("${surf.stormglass.base-url}")
    private String baseUrl;

    @Override
    public SurfConditions getCurrentConditions(double latitude, double longitude) {
        try {
            RestClient client = RestClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", apiKey)
                    .build();

            var response = client.get()
                    .uri("/weather/point?lat={lat}&lng={lng}&params={params}&source=sg",
                            latitude, longitude, PARAMS)
                    .retrieve()
                    .body(Map.class);

            return parse(response);
        } catch (Exception e) {
            log.error("Erro ao buscar dados do Stormglass lat={} lng={}", latitude, longitude, e);
            throw new SurfDataUnavailableException("Stormglass indisponível", e);
        }
    }

    @SuppressWarnings("unchecked")
    private SurfConditions parse(Map<?, ?> response) {
        var hours = (List<Map<String, Object>>) response.get("hours");
        if (hours == null || hours.isEmpty()) {
            throw new SurfDataUnavailableException("Nenhum dado retornado pelo Stormglass");
        }

        var hour = hours.get(0);
        return new SurfConditions(
                extractValue(hour, "waveHeight"),
                extractValue(hour, "wavePeriod"),
                extractValue(hour, "windSpeed"),
                extractValue(hour, "windDirection"),
                extractValue(hour, "swellDirection"),
                extractValue(hour, "swellHeight"),
                extractValue(hour, "swellPeriod"),
                LocalDateTime.now()
        );
    }

    @SuppressWarnings("unchecked")
    private double extractValue(Map<String, Object> hour, String key) {
        var values = (Map<String, Object>) hour.get(key);
        if (values == null) return 0.0;
        Object sg = values.get("sg");
        return sg instanceof Number n ? n.doubleValue() : 0.0;
    }

    public static class SurfDataUnavailableException extends RuntimeException {
        public SurfDataUnavailableException(String message) { super(message); }
        public SurfDataUnavailableException(String message, Throwable cause) { super(message, cause); }
    }
}
