package com.artipro.service;

import com.artipro.exception.PubMedApiException;
import com.artipro.model.entity.Article;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class PubMedService {
    private final String BASE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;


    @Autowired
    public PubMedService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }


    //@Cacheable(value = "articles", key = "#query")
    public List<Article> searchArticles(String query) {
        try {
            // İlk olarak ID'leri alalım
            String searchUrl = BASE_URL + "esearch.fcgi?db=pmc&term=" + URLEncoder.encode(query, "UTF-8") +
                    "&retmax=100&retmode=json";

            ResponseEntity<String> searchResponse = restTemplate.getForEntity(searchUrl, String.class);
            JsonNode rootNode = objectMapper.readTree(searchResponse.getBody());
            JsonNode idList = rootNode.path("esearchresult").path("idlist");

            List<Article> articles = new ArrayList<>();

            if (idList.isArray()) {
                // Her bir ID için detay bilgilerini alalım
                for (JsonNode id : idList) {
                    String pmcId = id.asText();
                    String summaryUrl = BASE_URL + "esummary.fcgi?db=pmc&id=" + pmcId + "&retmode=json";

                    try {
                        ResponseEntity<String> summaryResponse = restTemplate.getForEntity(summaryUrl, String.class);
                        JsonNode summaryRoot = objectMapper.readTree(summaryResponse.getBody());
                        JsonNode result = summaryRoot.path("result").path(pmcId);

                        Article article = new Article();
                        article.setPmcId(pmcId);
                        article.setTitle(result.path("title").asText());
                        article.setArticleAbstract(result.path("abstract").asText());

                        // Yazarları ayarla
                        List<String> authors = new ArrayList<>();
                        JsonNode authorList = result.path("authors");
                        if (authorList.isArray()) {
                            for (JsonNode author : authorList) {
                                authors.add(author.path("name").asText());
                            }
                        }
                        article.setAuthors(authors);

                        // Dergi bilgisini ayarla
                        article.setJournal(result.path("fulljournalname").asText());

                        // Yayın tarihini ayarla
                        String pubDate = result.path("pubdate").asText();
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMM d");
                            article.setPublicationDate(LocalDate.parse(pubDate, formatter));
                        } catch (Exception e) {
                            log.warn("Tarih parse edilemedi: {}", pubDate);
                        }

                        // DOI bilgisini ayarla
                        article.setDoi(result.path("articleids")
                                .findValues("value")
                                .stream()
                                .filter(v -> v.asText().startsWith("10."))
                                .findFirst()
                                .map(JsonNode::asText)
                                .orElse(null));

                        // Anahtar kelimeleri ayarla
                        List<String> keywords = new ArrayList<>();
                        JsonNode keywordList = result.path("keywords");
                        if (keywordList.isArray()) {
                            for (JsonNode keyword : keywordList) {
                                keywords.add(keyword.asText());
                            }
                        }
                        article.setKeywords(keywords);

                        // Tam metin URL'sini ayarla
                        article.setFullTextUrl("https://www.ncbi.nlm.nih.gov/pmc/articles/PMC" + pmcId);

                        articles.add(article);

                        // Rate limiting için kısa bir bekleme
                        Thread.sleep(100);

                    } catch (Exception e) {
                        log.error("Article {} için detay bilgileri alınamadı: {}", pmcId, e.getMessage());
                    }
                }
            }

            return articles;

        } catch (Exception e) {
            log.error("PubMed API error: ", e);
            throw new PubMedApiException("Error fetching articles from PubMed", e);
        }
    }

    //@Cacheable(value = "article", key = "#pmcId")
    public Article getArticleDetails(String pmcId) {
        String url = BASE_URL + "efetch.fcgi?db=pmc&id=" + pmcId +
                "&retmode=xml";

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.info("PubMed API response for ID {}: {}", pmcId, response.getBody());
            // XML parse işlemleri ve Article nesnesine dönüştürme
            return new Article();
        } catch (Exception e) {
            log.error("PubMed API error: ", e);
            throw new PubMedApiException("Error fetching article details from PubMed", e);
        }
    }
}