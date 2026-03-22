package com.surfnow.domain.score;

import com.surfnow.domain.entity.Beach;
import org.springframework.stereotype.Component;

/**
 * Calcula o score de surf (0–10) para uma praia baseado nas condições atuais.
 *
 * Distribuição dos pontos:
 *   Altura da onda  → até 3 pts
 *   Período         → até 2 pts
 *   Vento           → até 3 pts
 *   Direção do swell→ até 2 pts
 *   Total máximo    = 10 pts
 */
@Component
public class SurfScoreCalculator {

    public double calculate(SurfConditions conditions, Beach beach) {
        double waveScore   = calculateWaveScore(conditions.waveHeight(), beach.getIdealWaveMin(), beach.getIdealWaveMax());
        double periodScore = calculatePeriodScore(conditions.wavePeriod());
        double windScore   = calculateWindScore(conditions.windSpeed(), conditions.windDirection(), beach.getIdealWindDirection());
        double swellScore  = calculateSwellScore(conditions.swellDirection(), beach.getBestSwellDirection());
        return Math.min(waveScore + periodScore + windScore + swellScore, 10.0);
    }

    // Altura da onda (0–3 pts)
    double calculateWaveScore(double height, double idealMin, double idealMax) {
        if (height >= idealMin && height <= idealMax) return 3.0;
        if (height < idealMin) {
            return idealMin == 0 ? 0 : Math.max(0, 3.0 * (height / idealMin));
        }
        // Onda grande demais: perde 1.5 pt por metro acima do máximo
        return Math.max(0, 3.0 - ((height - idealMax) * 1.5));
    }

    // Período da onda (0–2 pts): groundswell > windswell
    double calculatePeriodScore(double period) {
        if (period >= 12) return 2.0;
        if (period >= 8)  return 1.0;
        return 0.5;
    }

    // Vento (0–3 pts)
    double calculateWindScore(double speed, double windDirection, String idealWindDirection) {
        double base;
        if (speed < 5)       base = 2.0; // glass off
        else if (speed < 15) base = 1.5;
        else if (speed < 25) base = 1.0;
        else                 base = 0.5;

        if (isOffshore(windDirection, idealWindDirection))      base += 1.0;
        else if (isOnshore(windDirection, idealWindDirection))  base -= 1.0;

        return Math.max(0, Math.min(3.0, base));
    }

    // Direção do swell (0–2 pts)
    double calculateSwellScore(double swellDirection, String bestSwellDirection) {
        double diff = angleDiff(swellDirection, directionToDegrees(bestSwellDirection));
        if (diff <= 30) return 2.0;
        if (diff <= 60) return 1.0;
        return 0.0;
    }

    public String scoreLabel(double score) {
        if (score >= 8) return "Excelente";
        if (score >= 6) return "Bom";
        if (score >= 4) return "Regular";
        if (score >= 2) return "Fraco";
        return "Péssimo";
    }

    private boolean isOffshore(double windDir, String idealDir) {
        return angleDiff(windDir, directionToDegrees(idealDir)) <= 45;
    }

    private boolean isOnshore(double windDir, String idealDir) {
        double opposite = (directionToDegrees(idealDir) + 180) % 360;
        return angleDiff(windDir, opposite) <= 45;
    }

    private double angleDiff(double a, double b) {
        double diff = Math.abs(a - b) % 360;
        return diff > 180 ? 360 - diff : diff;
    }

    private double directionToDegrees(String direction) {
        return switch (direction.toUpperCase()) {
            case "N"  -> 0;
            case "NE" -> 45;
            case "E"  -> 90;
            case "SE" -> 135;
            case "S"  -> 180;
            case "SW" -> 225;
            case "W"  -> 270;
            case "NW" -> 315;
            default -> throw new IllegalArgumentException("Direção inválida: " + direction);
        };
    }
}
