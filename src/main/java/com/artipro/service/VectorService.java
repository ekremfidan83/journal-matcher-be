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
            Map<String, String> request = new HashMap<>();
            request.put("title", article.getTitle());
            request.put("abstract", article.getArticleAbstract());

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    VECTOR_SERVICE_URL,
                    request,
                    Map.class
            );

            List<Double> vectorList = (List<Double>) response.getBody().get("vector");
            return vectorList.stream().mapToDouble(Double::doubleValue).toArray();

        } catch (Exception e) {
            log.error("Error getting vector for article: " + article.getTitle(), e);
            throw new RuntimeException("Vector service error", e);
        }
    }
}