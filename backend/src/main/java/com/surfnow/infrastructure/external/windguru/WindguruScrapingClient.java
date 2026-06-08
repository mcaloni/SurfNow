package com.surfnow.infrastructure.external.windguru;

import com.surfnow.domain.score.SurfConditions;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fallback que extrai previsão do Windguru via scraping HTML de windguru.cz/{spotId}.
 * Baseado em https://github.com/Theov/WindguruForecastApi
 *
 * Ativo apenas quando credenciais Pro NÃO estão configuradas
 * (i.e., {@code surf.windguru.username} está vazio).
 *
 * Aviso: o scraping pode parar de funcionar se o Windguru alterar o layout do site.
 * Retorna {@link Optional#empty()} silenciosamente em qualquer falha.
 */
@Slf4j
@Component
@ConditionalOnExpression("'${surf.windguru.username:}'.length() == 0")
public class WindguruScrapingClient implements WindguruForecastPort {

    private static final Pattern FCST_PATTERN = Pattern.compile(
            "wg_fcst_tab_data_1\\s*=\\s*(\\{\"initstamp\".*?\\}\\});",
            Pattern.DOTALL
    );

    @Value("${surf.windguru.base-url}")
    private String baseUrl;

    @Override
    public Optional<SurfConditions> fetch(int spotId) {
        try {
            Document doc = Jsoup.connect(baseUrl + "/" + spotId)
                    .userAgent("Mozilla/5.0 (compatible; SurfNow/1.0)")
                    .timeout(8000)
                    .get();

            Matcher matcher = FCST_PATTERN.matcher(doc.html());
            if (!matcher.find()) {
                log.warn("WindguruScrapingClient: padrão JS não encontrado para spot {}", spotId);
                return Optional.empty();
            }

            return Optional.of(parse(matcher.group(1), spotId));

        } catch (Exception e) {
            log.warn("WindguruScrapingClient: falha ao buscar spot {} — {}", spotId, e.getMessage());
            return Optional.empty();
        }
    }

    private SurfConditions parse(String json, int spotId) {
        double windSpeed   = extractFirstDouble(json, "\"WINDSPD\"") * 1.852;  // nós → km/h
        double windDir     = extractFirstDouble(json, "\"WINDDIR\"");
        double swellHeight = extractFirstDouble(json, "\"SMER\"");
        double swellPeriod = extractFirstDouble(json, "\"SMPER\"");
        double swellDir    = extractFirstDouble(json, "\"SDIR\"");

        log.debug("WindguruScrapingClient: spot {} → wind={} km/h, swell={}m", spotId,
                String.format("%.1f", windSpeed), swellHeight);

        return new SurfConditions(
                0, 0,  // waveHeight/wavePeriod: Windguru não fornece
                windSpeed, windDir,
                swellDir, swellHeight, swellPeriod,
                LocalDateTime.now()
        );
    }

    private double extractFirstDouble(String json, String key) {
        int keyIdx = json.indexOf(key);
        if (keyIdx < 0) return 0.0;
        int bracketIdx = json.indexOf('[', keyIdx);
        if (bracketIdx < 0) return 0.0;
        int commaOrClose = json.indexOf(',', bracketIdx + 1);
        int closeIdx = json.indexOf(']', bracketIdx + 1);
        int end = commaOrClose > 0 && commaOrClose < closeIdx ? commaOrClose : closeIdx;
        if (end < 0) return 0.0;
        try {
            return Double.parseDouble(json.substring(bracketIdx + 1, end).trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
