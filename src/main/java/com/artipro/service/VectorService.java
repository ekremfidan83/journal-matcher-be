package com.artipro.service;

import com.artipro.model.entity.Article;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class VectorService {
    private final String VECTOR_SERVICE_URL = "http://localhost:8000/vectorize";
    private final RestTemplate restTemplate;

    @Autowired
    public VectorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public double[] getVector(Article article) {
        try {
            // Title ve abstract'ı birleştir
            String combinedText = article.getTitle() + " " +
                    (article.getArticleAbstract() != null ? article.getArticleAbstract() : "");

            // Request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("text", combinedText);

            log.info("Sending text to vectorize: {}", combinedText.substring(0, Math.min(100, combinedText.length())));

            // HTTP isteği
            ResponseEntity<double[]> response = restTemplate.postForEntity(
                    "http://localhost:8000/vectorize",
                    requestBody,
                    double[].class
            );

            log.info("Vector generated for article: {}", article.getTitle());
            return response.getBody();

        } catch (Exception e) {
            log.error("Error getting vector for article: " + article.getTitle(), e);
            throw new RuntimeException("Error getting vector", e);
        }
    }
}