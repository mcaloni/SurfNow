package com.surfnow.domain.repository;

import com.surfnow.domain.entity.SurfReport;

import java.util.List;
import java.util.UUID;

public interface SurfReportRepository {
    List<SurfReport> findRecentByBeachId(UUID beachId, int limit);
    SurfReport save(SurfReport report);
}
