package com.artipro.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JournalMatch {
    private String journalName;
    private double similarity;
    private String issn;
}