package com.surfnow.infrastructure.persistence;

import com.surfnow.domain.entity.SurfReport;
import com.surfnow.domain.repository.SurfReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SurfReportRepositoryImpl implements SurfReportRepository {

    private final SurfReportJpaRepository jpa;

    @Override
    public List<SurfReport> findRecentByBeachId(UUID beachId, int limit) {
        return jpa.findByBeachIdOrderByCreatedAtDesc(beachId, PageRequest.of(0, limit));
    }

    @Override
    public SurfReport save(SurfReport report) {
        return jpa.save(report);
    }
}
