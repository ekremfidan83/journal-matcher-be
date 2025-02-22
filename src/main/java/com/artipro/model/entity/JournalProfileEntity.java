package com.artipro.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
@Entity
@Table(name = "journal_profiles")
@Data
public class JournalProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String journalName;

    @Column(unique = true)
    private String issn;

    @Column(name = "vector", columnDefinition = "vector(384)")
    private String vectorString;  // vektörü string olarak tut

    @Transient  // bu alan veritabanına kaydedilmeyecek
    private double[] vector;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(nullable = true)
    private Double impactFactor;

    @Column(columnDefinition = "text")
    private String scope;

    @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL)
    private List<JournalArticleEntity> articles;

    public double[] getVector() {
        if (vectorString == null) return null;
        // String'den double[] dönüşümü
        String[] values = vectorString.substring(1, vectorString.length() - 1).split(",");
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Double.parseDouble(values[i].trim());
        }
        return result;
    }

    public void setVector(double[] vector) {
        this.vector = vector;
        // double[]'den String dönüşümü
        if (vector != null) {
            this.vectorString = Arrays.toString(vector);
        }
    }
}
