package com.artipro.model;

import com.artipro.model.entity.Article;
import lombok.Data;

import java.util.List;

@Data
public class JournalProfile {
    private String name;
    private String issn;
    private List<Article> recentArticles;
    private String scope;
    private String impactFactor;
    private List<String> commonKeywords;
    // diğer özellikler...
}