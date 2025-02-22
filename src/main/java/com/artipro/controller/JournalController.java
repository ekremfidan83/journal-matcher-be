package com.artipro.controller;

import com.artipro.model.ArticleSubmission;
import com.artipro.model.JournalMatch;
import com.artipro.model.JournalProfile;
import com.artipro.service.JournalProfileService;
import com.artipro.service.VectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journals")
@Slf4j
public class JournalController {

    private final JournalProfileService journalProfileService;

    private final VectorService vectorService;

    @Autowired
    public JournalController(JournalProfileService journalProfileService, VectorService vectorService) {
        this.journalProfileService = journalProfileService;
        this.vectorService = vectorService;
    }

    @GetMapping("/profiles")
    public ResponseEntity<List<JournalProfile>> getJournalProfiles() {
        try {
            List<JournalProfile> profiles = journalProfileService.collectJournalProfiles();
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            log.error("Error collecting journal profiles", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/match")
    public ResponseEntity<List<JournalMatch>> findMatchingJournals(@RequestBody ArticleSubmission submission) {
        try {
            // 1. Gelen makaleyi vektörize et
            double[] articleVector = vectorService.getVector(submission.toArticle());

            // 2. Dergi profilleriyle karşılaştır
            List<JournalMatch> matches = journalProfileService.findMatchingJournals(articleVector);

            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            log.error("Error matching article with journals", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Journal API is working!");
    }
}
