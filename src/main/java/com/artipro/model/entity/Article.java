package com.artipro.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @Id
    private String pmcId;

    private String title;

    @Column(length = 4000)
    private String articleAbstract;

    @ElementCollection
    private List<String> authors;

    private String journal;

    private LocalDate publicationDate;

    private String doi;

    @ElementCollection
    private List<String> keywords;

    @Column(length = 1000)
    private String fullTextUrl;
}