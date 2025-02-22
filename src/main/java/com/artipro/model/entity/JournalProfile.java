package com.artipro.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "journal_profiles")
@Data
public class JournalProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String journalName;

    @Column(unique = true)
    private String issn;

    @Column(columnDefinition = "vector(384)")
    private double[] vector;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(nullable = true)
    private Double impactFactor;

    @Column(columnDefinition = "text")
    private String scope;

    @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL)
    private List<JournalArticle> articles;
}
