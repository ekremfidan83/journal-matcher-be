package com.artipro.repository;

import com.artipro.model.entity.JournalArticleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalArticleRepository extends JpaRepository<JournalArticleEntity, Long> {

    @Query(value = "SELECT * FROM journal_articles WHERE journal_id = :journalId",
            nativeQuery = true)
    List<JournalArticleEntity> findArticlesByJournalId(@Param("journalId") Long journalId);

}
