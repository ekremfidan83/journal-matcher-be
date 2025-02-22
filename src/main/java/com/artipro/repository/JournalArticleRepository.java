package com.artipro.repository;

import com.artipro.model.entity.JournalArticle;
import com.artipro.model.entity.JournalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JournalArticleRepository extends JpaRepository<JournalArticle, Long> {
    List<JournalArticle> findByJournalOrderByPublishedDateDesc(JournalProfile journal);
    Optional<JournalArticle> findByPmid(String pmid);

}
