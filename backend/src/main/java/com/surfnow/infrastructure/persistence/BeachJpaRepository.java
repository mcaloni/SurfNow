package com.surfnow.infrastructure.persistence;

import com.surfnow.domain.entity.Beach;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface BeachJpaRepository extends JpaRepository<Beach, UUID> {
    List<Beach> findByState(String state);
}
