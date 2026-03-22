package com.surfnow.domain.score;

import com.surfnow.domain.entity.Beach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SurfScoreCalculatorTest {

    private SurfScoreCalculator calculator;
    private Beach maresias;

    @BeforeEach
    void setUp() {
        calculator = new SurfScoreCalculator();
        maresias = Beach.builder()
                .name("Maresias")
                .state("SP")
                .city("São Sebastião")
                .idealWaveMin(0.8)
                .idealWaveMax(2.5)
                .idealWindDirection("N")   // vento de Norte é offshore
                .bestSwellDirection("S")   // swell de Sul é o ideal
                .build();
    }

    @Nested
    @DisplayName("Condição ideal")
    class IdealConditions {

        @Test
        @DisplayName("Deve pontuar alto com condições perfeitas")
        void shouldScoreHighForPerfectConditions() {
            SurfConditions perfect = new SurfConditions(
                    1.5,  // onda dentro do ideal
                    14.0, // groundswell
                    5.0,  // vento leve
                    0.0,  // vento de Norte (offshore para Maresias)
                    180.0,// swell de Sul (ideal)
                    1.2, 14.0,
                    LocalDateTime.now()
            );

            double score = calculator.calculate(perfect, maresias);

            assertThat(score).isGreaterThanOrEqualTo(8.0);
        }

        @Test
        @DisplayName("Score nunca deve exceder 10")
        void shouldNeverExceed10() {
            SurfConditions overPerfect = new SurfConditions(
                    2.0, 20.0, 2.0, 0.0, 180.0, 2.0, 20.0, LocalDateTime.now());

            assertThat(calculator.calculate(overPerfect, maresias)).isLessThanOrEqualTo(10.0);
        }
    }

    @Nested
    @DisplayName("Altura da onda")
    class WaveHeight {

        @Test
        @DisplayName("Onda dentro do range ideal → 3 pontos")
        void shouldScore3ForWaveInIdealRange() {
            assertThat(calculator.calculateWaveScore(1.5, 0.8, 2.5)).isEqualTo(3.0);
        }

        @Test
        @DisplayName("Onda muito pequena → menos de 3 pontos")
        void shouldScoreLessForSmallWave() {
            assertThat(calculator.calculateWaveScore(0.3, 0.8, 2.5)).isLessThan(2.0);
        }

        @Test
        @DisplayName("Onda muito grande → perde pontos")
        void shouldScoreLessForHugeWave() {
            assertThat(calculator.calculateWaveScore(5.0, 0.8, 2.5)).isLessThan(2.0);
        }

        @Test
        @DisplayName("Onda absurdamente grande → pode zerar")
        void shouldScoreZeroForExtremelyBigWave() {
            assertThat(calculator.calculateWaveScore(10.0, 0.8, 2.5)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Período da onda")
    class WavePeriod {

        @Test
        @DisplayName("Groundswell (>= 12s) → 2 pontos")
        void shouldScore2ForGroundswell() {
            assertThat(calculator.calculatePeriodScore(14.0)).isEqualTo(2.0);
        }

        @Test
        @DisplayName("Swell médio (8–12s) → 1 ponto")
        void shouldScore1ForMediumPeriod() {
            assertThat(calculator.calculatePeriodScore(10.0)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Wind chop (< 8s) → 0.5 pontos")
        void shouldScore05ForWindChop() {
            assertThat(calculator.calculatePeriodScore(5.0)).isEqualTo(0.5);
        }
    }

    @Nested
    @DisplayName("Vento")
    class Wind {

        @Test
        @DisplayName("Vento leve offshore → máximo 3 pontos")
        void shouldScoreMaxForLightOffshore() {
            // Vento de Norte (0°) é offshore para Maresias
            double score = calculator.calculateWindScore(3.0, 0.0, "N");
            assertThat(score).isEqualTo(3.0);
        }

        @Test
        @DisplayName("Vento forte onshore → pontuação baixa")
        void shouldScoreLowForStrongOnshore() {
            // Vento de Sul (180°) = onshore para praia com offshore N
            double score = calculator.calculateWindScore(30.0, 180.0, "N");
            assertThat(score).isLessThan(1.0);
        }

        @Test
        @DisplayName("Glass off (< 5 km/h) + offshore → 3 pontos")
        void shouldScore3ForGlassOffOffshore() {
            double score = calculator.calculateWindScore(2.0, 0.0, "N");
            assertThat(score).isEqualTo(3.0);
        }
    }

    @Nested
    @DisplayName("Direção do swell")
    class SwellDirection {

        @Test
        @DisplayName("Swell na direção ideal → 2 pontos")
        void shouldScore2ForIdealSwellDirection() {
            assertThat(calculator.calculateSwellScore(180.0, "S")).isEqualTo(2.0);
        }

        @Test
        @DisplayName("Swell próximo ao ideal (< 60°) → 1 ponto")
        void shouldScore1ForCloseSwellDirection() {
            // 135° de swell (SE) vs ideal 180° (S) = diff de 45°
            assertThat(calculator.calculateSwellScore(135.0, "S")).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Swell oposto ao ideal → 0 pontos")
        void shouldScore0ForWrongSwellDirection() {
            // 0° de swell (N) vs ideal 180° (S) = diff de 180°
            assertThat(calculator.calculateSwellScore(0.0, "S")).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Labels de score")
    class ScoreLabels {

        @Test void excellent() { assertThat(calculator.scoreLabel(9.0)).isEqualTo("Excelente"); }
        @Test void good()      { assertThat(calculator.scoreLabel(7.0)).isEqualTo("Bom"); }
        @Test void regular()   { assertThat(calculator.scoreLabel(5.0)).isEqualTo("Regular"); }
        @Test void weak()      { assertThat(calculator.scoreLabel(3.0)).isEqualTo("Fraco"); }
        @Test void bad()       { assertThat(calculator.scoreLabel(1.0)).isEqualTo("Péssimo"); }
    }
}
