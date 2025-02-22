package com.artipro.service;

import com.artipro.model.JournalInfo;
import com.artipro.model.JournalMatch;
import com.artipro.model.JournalProfile;
import com.artipro.model.entity.Article;
import com.artipro.model.entity.JournalArticleEntity;
import com.artipro.model.entity.JournalProfileEntity;
import com.artipro.repository.JournalArticleRepository;
import com.artipro.repository.JournalProfileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JournalProfileService {
    private final String BASE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
    private final RestTemplate restTemplate;
    private final VectorService vectorService;
    private final JournalProfileRepository profileRepository;
    private final JournalArticleRepository articleRepository;

    private final List<JournalInfo> targetJournals = Arrays.asList(
            new JournalInfo("The Lancet", "0140-6736"),
            new JournalInfo("New England Journal of Medicine", "0028-4793"),
            new JournalInfo("JAMA", "0098-7484"),
            new JournalInfo("BMJ", "0959-8138"),
            new JournalInfo("Nature Medicine", "1078-8956")
    );

    @Autowired
    public JournalProfileService(RestTemplate restTemplate, VectorService vectorService,
                                 JournalProfileRepository profileRepository, JournalArticleRepository articleRepository) {
        this.restTemplate = restTemplate;
        this.vectorService = vectorService;
        this.profileRepository = profileRepository;
        this.articleRepository = articleRepository;
    }


    public List<JournalProfile> collectJournalProfiles() {
        List<JournalProfile> profiles = new ArrayList<>();
        log.info("Starting to collect journal profiles");

        for (JournalInfo journal : targetJournals) {
            try {
                // Model oluştur
                JournalProfile profile = buildJournalProfile(journal);
                profiles.add(profile);

                // Dergi entity'sini oluştur ve kaydet
                JournalProfileEntity journalProfileEntity = new JournalProfileEntity();
                journalProfileEntity.setJournalName(profile.getName());
                journalProfileEntity.setIssn(profile.getIssn());
                journalProfileEntity.setLastUpdated(LocalDateTime.now());

                // Dergiyi kaydet ve kaydedilen entity'yi al
                JournalProfileEntity savedProfile = profileRepository.save(journalProfileEntity);
                log.info("Saved journal profile: {}", savedProfile.getJournalName());

                // Makaleleri kaydet ve vektörize et
                List<JournalArticleEntity> articles = new ArrayList<>();
                for (Article article : profile.getRecentArticles()) {
                    JournalArticleEntity articleEntity = new JournalArticleEntity();
                    articleEntity.setTitle(article.getTitle());
                    articleEntity.setArticleAbstract(article.getArticleAbstract());
                    articleEntity.setPmid(article.getPmcId());
                    //articleEntity.setPublishedDate(article.getPublicationDate());
                    articleEntity.setJournal(savedProfile);

                    // Makaleyi vektörize et
                    try {
                        double[] vector = vectorService.getVector(article);
                        articleEntity.setVector(vector);
                        log.info("Vector generated for article: {}", article.getTitle());
                    } catch (Exception e) {
                        log.error("Error generating vector for article: {}", article.getTitle(), e);
                    }

                    articles.add(articleEntity);
                }

                // Tüm makaleleri toplu kaydet
                articleRepository.saveAll(articles);
                log.info("Saved {} articles with vectors for journal: {}",
                        articles.size(), savedProfile.getJournalName());

            } catch (Exception e) {
                log.error("Error collecting profile for journal: " + journal.getName(), e);
            }
        }

        return profiles;
    }

    public List<JournalMatch> findMatchingJournals(double[] articleVector) {
        List<JournalMatch> matches = new ArrayList<>();
        List<JournalProfileEntity> journals = profileRepository.findAll();

        log.info("Found {} journals in database", journals.size());
        log.info("Input article vector size: {}", articleVector.length);

        for (JournalProfileEntity journal : journals) {
            try {
                Long journalId = journal.getId();
                log.info("Querying articles for journal ID: {}", journalId);
                List<JournalArticleEntity> articles = articleRepository.findArticlesByJournalId(journalId);
                log.info("Journal: {}, Found {} articles", journal.getJournalName(), articles.size());

                if (articles.isEmpty()) {
                    log.warn("No articles found for journal: {}", journal.getJournalName());
                    continue;
                }

                // Vektör null kontrolü
                articles = articles.stream()
                        .filter(a -> a.getVector() != null)
                        .collect(Collectors.toList());

                if (articles.isEmpty()) {
                    log.warn("No articles with vectors found for journal: {}", journal.getJournalName());
                    continue;
                }

                log.info("Processing {} articles with vectors for journal: {}",
                        articles.size(), journal.getJournalName());

                // Her makalenin vektör boyutunu kontrol et
                articles.forEach(article ->
                        log.info("Article: {}, Vector size: {}",
                                article.getTitle(),
                                article.getVector() != null ? article.getVector().length : "null")
                );

                double[] journalVector = calculateJournalVector(articles);
                double similarity = calculateCosineSimilarity(articleVector, journalVector);

                matches.add(new JournalMatch(
                        journal.getJournalName(),
                        similarity,
                        journal.getIssn()
                ));

            } catch (Exception e) {
                log.error("Error calculating similarity for journal: " + journal.getJournalName(), e);
                log.error("Exception details: ", e);  // Stack trace'i göster
            }
        }

        return matches.stream()
                .sorted(Comparator.comparing(JournalMatch::getSimilarity).reversed())
                .collect(Collectors.toList());
    }

    private double[] calculateJournalVector(List<JournalArticleEntity> articles) {
        try {
            // İlk vektörün boyutunu al
            int vectorSize = articles.get(0).getVector().length;
            log.info("Vector size for calculation: {}", vectorSize);

            double[] meanVector = new double[vectorSize];
            int validVectorCount = 0;

            for (JournalArticleEntity article : articles) {
                if (article.getVector() != null) {
                    double[] vector = article.getVector();
                    for (int i = 0; i < vectorSize; i++) {
                        meanVector[i] += vector[i];
                    }
                    validVectorCount++;
                    log.info("Added vector from article: {}", article.getTitle());
                }
            }

            // Ortalama al
            if (validVectorCount > 0) {
                for (int i = 0; i < vectorSize; i++) {
                    meanVector[i] /= validVectorCount;
                }
                log.info("Calculated mean vector from {} articles", validVectorCount);
                return meanVector;
            } else {
                throw new RuntimeException("No valid vectors found in articles");
            }
        } catch (Exception e) {
            log.error("Error in calculateJournalVector: {}", e.getMessage());
            throw e;
        }
    }

    private double calculateCosineSimilarity(double[] vectorA, double[] vectorB) {
        try {
            if (vectorA.length != vectorB.length) {
                log.error("Vector dimensions don't match: A={}, B={}", vectorA.length, vectorB.length);
                throw new IllegalArgumentException("Vector dimensions must match");
            }

            double dotProduct = 0.0;
            double normA = 0.0;
            double normB = 0.0;

            for (int i = 0; i < vectorA.length; i++) {
                dotProduct += vectorA[i] * vectorB[i];
                normA += vectorA[i] * vectorA[i];
                normB += vectorB[i] * vectorB[i];
            }

            if (normA == 0 || normB == 0) {
                log.warn("Zero magnitude vector detected");
                return 0.0;
            }

            double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
            log.info("Calculated similarity: {}", similarity);
            return similarity;

        } catch (Exception e) {
            log.error("Error in calculateCosineSimilarity: {}", e.getMessage());
            throw e;
        }
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