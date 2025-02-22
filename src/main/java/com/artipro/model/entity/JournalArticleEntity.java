package com.artipro.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Arrays;

@Entity
@Table(name = "journal_articles")
@Data
public class JournalArticleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "journal_id", nullable = false)
    private JournalProfileEntity journal;

    @Column(columnDefinition = "text")
    private String title;

    @Column(name = "article_abstract", columnDefinition = "text")
    private String articleAbstract;

    @Column(name = "vector", columnDefinition = "vector(384)")
    private String vectorString;  // vektörü string olarak tut

    @Transient  // bu alan veritabanına kaydedilmeyecek
    private double[] vector;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Column(columnDefinition = "text")
    private String pmid;

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