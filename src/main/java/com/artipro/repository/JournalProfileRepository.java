package com.artipro.repository;

import com.artipro.model.entity.JournalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JournalProfileRepository extends JpaRepository<JournalProfile, Long> {
    Optional<JournalProfile> findByIssn(String issn);
    List<JournalProfile> findByJournalNameContainingIgnoreCase(String name);
}
