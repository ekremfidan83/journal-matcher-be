package com.artipro.service;

import com.artipro.model.JournalInfo;
import com.artipro.model.JournalMatch;
import com.artipro.model.JournalProfile;
import com.artipro.model.entity.Article;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JournalProfileService {
    private final String BASE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
    private final RestTemplate restTemplate;

    private final VectorService vectorService;

    private final List<JournalInfo> targetJournals = Arrays.asList(
            new JournalInfo("The Lancet", "0140-6736"),
            new JournalInfo("New England Journal of Medicine", "0028-4793"),
            new JournalInfo("JAMA", "0098-7484"),
            new JournalInfo("BMJ", "0959-8138"),
            new JournalInfo("Nature Medicine", "1078-8956")
    );

    @Autowired
    public JournalProfileService(RestTemplate restTemplate, VectorService vectorService) {
        this.restTemplate = restTemplate;
        this.vectorService = vectorService;
    }

    public List<JournalProfile> collectJournalProfiles() {
        List<JournalProfile> profiles = new ArrayList<>();
        log.info("Starting to collect journal profiles");

        for (JournalInfo journal : targetJournals) {
            try {
                JournalProfile profile = buildJournalProfile(journal);
                profiles.add(profile);
            } catch (Exception e) {
                log.error("Error collecting profile for journal: " + journal.getName(), e);
            }
        }

        log.info("Found {} profiles", profiles.size());
        return profiles;
    }

    public List<JournalMatch> findMatchingJournals(double[] articleVector) {
        List<JournalMatch> matches = new ArrayList<>();
        List<JournalProfile> profiles = collectJournalProfiles();

        for (JournalProfile profile : profiles) {
            try {
                // Her dergi için son makalelerin vektörlerinin ortalamasını al
                double[] journalVector = calculateJournalVector(profile);
                log.info("Calculated vector for journal: {}", profile.getName());
                // Benzerlik skorunu hesapla
                double similarity = calculateCosineSimilarity(articleVector, journalVector);
                log.info("Similarity with {}: {}", profile.getName(), similarity);
                matches.add(new JournalMatch(
                        profile.getName(),
                        similarity,
                        profile.getIssn()
                ));
            } catch (Exception e) {
                log.error("Error calculating similarity for journal: " + profile.getName(), e);
            }
        }

        // Benzerlik skoruna göre sırala (en yüksekten düşüğe)
        matches.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));

        // En iyi 3 eşleşmeyi döndür
        return matches.stream().limit(3).collect(Collectors.toList());
    }

    private double[] calculateJournalVector(JournalProfile profile) {
        // Dergi profilindeki makalelerin vektörlerini hesapla
        List<double[]> articleVectors = profile.getRecentArticles().stream()
                .map(vectorService::getVector)
                .collect(Collectors.toList());

        // Vektörlerin ortalamasını al
        int vectorSize = articleVectors.get(0).length;
        double[] meanVector = new double[vectorSize];

        for (double[] vector : articleVectors) {
            for (int i = 0; i < vectorSize; i++) {
                meanVector[i] += vector[i];
            }
        }

        for (int i = 0; i < vectorSize; i++) {
            meanVector[i] /= articleVectors.size();
        }

        return meanVector;
    }

    private double calculateCosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private JournalProfile buildJournalProfile(JournalInfo journalInfo) {
        try {
            // 1. Dergi için son makaleleri çek
            String searchUrl = BASE_URL + "esearch.fcgi?db=pubmed" +
                    "&term=" + URLEncoder.encode(journalInfo.getName() + "[Journal]", "UTF-8") +
                    "&retmax=20&retmode=json";

            ResponseEntity<String> searchResponse = restTemplate.getForEntity(searchUrl, String.class);
            JsonNode rootNode = new ObjectMapper().readTree(searchResponse.getBody());
            JsonNode idList = rootNode.path("esearchresult").path("idlist");

            // 2. Profil oluştur
            JournalProfile profile = new JournalProfile();
            profile.setName(journalInfo.getName());
            profile.setIssn(journalInfo.getIssn());
            profile.setRecentArticles(new ArrayList<>());

            // 3. Her makale ID'si için detayları çek
            if (idList.isArray()) {
                for (JsonNode id : idList) {
                    String articleId = id.asText();
                    Article article = fetchArticleDetails(articleId);
                    if (article != null) {
                        profile.getRecentArticles().add(article);
                    }
                    // Rate limiting
                    Thread.sleep(100);
                }
            }
            log.info("Built profile for journal: {}", journalInfo.getName());
            return profile;

        } catch (Exception e) {
            log.error("Error building profile for journal: " + journalInfo.getName(), e);
            throw new RuntimeException("Error building journal profile", e);
        }
    }

    private Article fetchArticleDetails(String pmid) {
        try {
            String detailUrl = BASE_URL + "esummary.fcgi?db=pubmed&id=" + pmid + "&retmode=json";
            ResponseEntity<String> detailResponse = restTemplate.getForEntity(detailUrl, String.class);

            // Parse article details
            JsonNode rootNode = new ObjectMapper().readTree(detailResponse.getBody());
            JsonNode result = rootNode.path("result").path(pmid);

            Article article = new Article();
            //article.setPmid(pmid);
            article.setTitle(result.path("title").asText());
            article.setArticleAbstract(result.path("abstract").asText());

            return article;

        } catch (Exception e) {
            log.error("Error fetching article details for PMID: " + pmid, e);
            return null;
        }
    }
}