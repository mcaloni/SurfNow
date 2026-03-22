package com.surfnow.api.dto;

import com.surfnow.domain.score.CrowdLevel;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SurfReportRequest(
        @NotNull UUID beachId,
        @NotNull CrowdLevel crowdLevel,
        String photoUrl,
        String description
) {}
