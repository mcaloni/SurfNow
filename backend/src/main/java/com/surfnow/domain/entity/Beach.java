package com.surfnow.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "beaches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Beach {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    /** Altura mínima ideal da onda (em metros) */
    @Column(nullable = false)
    private double idealWaveMin;

    /** Altura máxima ideal da onda (em metros) */
    @Column(nullable = false)
    private double idealWaveMax;

    /**
     * Direção do vento offshore para essa praia.
     * Ex: "N" = vento de Norte é offshore (praia voltada pro sul)
     */
    @Column(nullable = false, length = 2)
    private String idealWindDirection;

    /**
     * Melhor direção de swell para essa praia.
     * Ex: "S" = swell de Sul é o ideal
     */
    @Column(nullable = false, length = 2)
    private String bestSwellDirection;

    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "windguru_spot_id")
    private Integer windguruSpotId;
}
