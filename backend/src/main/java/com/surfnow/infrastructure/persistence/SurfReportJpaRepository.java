package com.surfnow.infrastructure.persistence;

import com.surfnow.domain.entity.SurfReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SurfReportJpaRepository extends JpaRepository<SurfReport, UUID> {
    List<SurfReport> findByBeachIdOrderByCreatedAtDesc(UUID beachId, Pageable pageable);
}
