package com.surfnow.domain.repository;

import com.surfnow.domain.entity.Beach;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeachRepository {
    List<Beach> findAll();
    List<Beach> findByState(String state);
    Optional<Beach> findById(UUID id);
    Beach save(Beach beach);
}
