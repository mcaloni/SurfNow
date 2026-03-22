package com.surfnow.domain.score;

public enum CrowdLevel {
    EMPTY,
    LOW,
    MEDIUM,
    HIGH,
    PACKED;

    public String toPortuguese() {
        return switch (this) {
            case EMPTY  -> "Vazia";
            case LOW    -> "Pouca gente";
            case MEDIUM -> "Razoável";
            case HIGH   -> "Cheia";
            case PACKED -> "Lotada";
        };
    }
}
