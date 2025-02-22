package com.artipro.repository;

import com.artipro.model.entity.JournalProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JournalProfileRepository extends JpaRepository<JournalProfileEntity, Long> {
    Optional<JournalProfileEntity> findByIssn(String issn);
    List<JournalProfileEntity> findByJournalNameContainingIgnoreCase(String name);
}
