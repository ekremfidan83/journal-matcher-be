package com.artipro.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "journal_articles")
@Data
public class JournalArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "journal_id", nullable = false)
    private JournalProfile journal;

    @Column(columnDefinition = "text")
    private String title;

    @Column(name = "article_abstract", columnDefinition = "text")
    private String articleAbstract;

    @Column(columnDefinition = "vector(384)")
    private double[] vector;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Column(columnDefinition = "text")
    private String pmid;
}