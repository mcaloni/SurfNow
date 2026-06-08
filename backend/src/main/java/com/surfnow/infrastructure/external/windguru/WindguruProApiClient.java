package com.surfnow.infrastructure.external.windguru;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.surfnow.domain.score.SurfConditions;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cliente Windguru Pro com autenticação por sessão.
 *
 * Fluxo:
 *   1. Login em windguru.cz com usuário e senha principal → obtém cookies de sessão
 *   2. Fetcha windguru.cz/{spotId} autenticado → extrai dados JS da página Pro
 *
 * Ativo apenas quando {@code surf.windguru.username} não está vazio.
 * Retorna {@link Optional#empty()} silenciosamente em qualquer falha.
 */
@Slf4j
@Component
@ConditionalOnExpression("'${surf.windguru.username:}'.length() > 0")
public class WindguruProApiClient implements WindguruForecastPort {

    private static final String BASE_URL = "https://www.windguru.cz";

    // Padrão para extrair dados de previsão do JS inline da página
    private static final Pattern FCST_PATTERN = Pattern.compile(
            "wg_fcst_tab_data_1\\s*=\\s*(\\{.*?\\});\\s*(?:wg_fcst|var |</script>)",
            Pattern.DOTALL
    );

    @Value("${surf.windguru.username}")
    private String username;

    @Value("${surf.windguru.password}")
    private String password;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, String> sessionCookies = new ConcurrentHashMap<>();

    @Override
    public Optional<SurfConditions> fetch(int spotId) {
        try {
            ensureLoggedIn();
            String response = fetchSpotPage(spotId);

            if (response == null || response.isBlank()) return Optional.empty();

            // Se a resposta já é JSON direto
            if (response.trim().startsWith("{")) {
                JsonNode check = objectMapper.readTree(response);
                if ("error".equals(check.path("return").asText())) {
                    log.warn("WindguruProApiClient: spot {} — API retornou erro: {}",
                            spotId, check.path("message").asText());
                    return Optional.empty();
                }
                return Optional.of(parseJson(response, spotId));
            }

            // Fallback: tentar extrair do HTML via regex
            Matcher matcher = FCST_PATTERN.matcher(response);
            if (!matcher.find()) {
                log.warn("WindguruProApiClient: dados não encontrados para spot {}", spotId);
                return Optional.empty();
            }

            return Optional.of(parseJson(matcher.group(1), spotId));

        } catch (Exception e) {
            log.warn("WindguruProApiClient: falha ao buscar spot {} — {}", spotId, e.getMessage());
            return Optional.empty();
        }
    }

    private synchronized void ensureLoggedIn() throws Exception {
        if (!sessionCookies.isEmpty()) return;

        log.info("WindguruProApiClient: realizando login com usuário {}", username);

        Connection.Response home = Jsoup.connect(BASE_URL + "/")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .method(Connection.Method.GET)
                .execute();

        Map<String, String> cookies = new ConcurrentHashMap<>(home.cookies());

        Connection.Response loginResp = Jsoup.connect(BASE_URL + "/")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .cookies(cookies)
                .data("login", username)
                .data("password", password)
                .data("submit_login", "1")
                .method(Connection.Method.POST)
                .execute();

        cookies.putAll(loginResp.cookies());
        sessionCookies.putAll(cookies);

        log.info("WindguruProApiClient: login concluído, {} cookies", sessionCookies.size());
    }

    private String fetchSpotPage(int spotId) throws Exception {
        // Tenta endpoint interno AJAX que o site usa para carregar previsões
        String apiResp = Jsoup.connect(BASE_URL + "/int/iapi.php")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(12000)
                .cookies(sessionCookies)
                .data("q", "forecast")
                .data("id_spot", String.valueOf(spotId))
                .data("m", "GFS")
                .data("lng", "en")
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .method(Connection.Method.GET)
                .execute()
                .body();

        log.info("WindguruProApiClient: iapi.php spot {} resposta (200 chars): {}",
                spotId, apiResp.substring(0, Math.min(200, apiResp.length())).replaceAll("\\s+", " "));

        return apiResp;
    }

    private SurfConditions parseJson(String json, int spotId) throws Exception {
        JsonNode root = objectMapper.readTree(json);

        double windSpeed   = firstDouble(root, "WINDSPD") * 1.852;
        double windDir     = firstDouble(root, "WINDDIR");
        double swellHeight = firstDouble(root, "SMER");
        double swellPeriod = firstDouble(root, "SMPER");
        double swellDir    = firstDouble(root, "SDIR");

        log.info("WindguruProApiClient: spot {} → wind={} km/h {}°, swell={}m {}s {}°",
                spotId, String.format("%.1f", windSpeed), (int) windDir,
                swellHeight, (int) swellPeriod, (int) swellDir);

        return new SurfConditions(
                0, 0,
                windSpeed, windDir,
                swellDir, swellHeight, swellPeriod,
                LocalDateTime.now()
        );
    }

    private double firstDouble(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null || !node.isArray() || node.isEmpty()) return 0.0;
        JsonNode first = node.get(0);
        return first.isNull() ? 0.0 : first.asDouble();
    }
}
