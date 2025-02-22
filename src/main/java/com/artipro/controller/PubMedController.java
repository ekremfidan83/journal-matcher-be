package com.artipro.controller;


import com.artipro.model.entity.Article;
import com.artipro.service.PubMedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pubmed")
@Slf4j
public class PubMedController {

    private final PubMedService pubMedService;

    @Autowired
    public PubMedController(PubMedService pubMedService) {
        this.pubMedService = pubMedService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<Article>> searchArticles(@RequestParam String query) {
        return ResponseEntity.ok(pubMedService.searchArticles(query));
    }

    @GetMapping("/article/{pmcId}")
    public ResponseEntity<Article> getArticle(@PathVariable String pmcId) {
        return ResponseEntity.ok(pubMedService.getArticleDetails(pmcId));
    }
}