package com.surfnow.infrastructure.persistence;

import com.surfnow.domain.entity.Beach;
import com.surfnow.domain.repository.BeachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BeachRepositoryImpl implements BeachRepository {

    private final BeachJpaRepository jpa;

    @Override
    public List<Beach> findAll() {
        return jpa.findAll();
    }

    @Override
    public List<Beach> findByState(String state) {
        return jpa.findByState(state);
    }

    @Override
    public Optional<Beach> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Beach save(Beach beach) {
        return jpa.save(beach);
    }
}
