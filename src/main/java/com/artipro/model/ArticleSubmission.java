package com.artipro.model;

import com.artipro.model.entity.Article;
import lombok.Data;

@Data
public class ArticleSubmission {
    private String title;
    private String articleAbstract;

    public Article toArticle() {
        Article article = new Article();
        article.setTitle(title);
        article.setArticleAbstract(articleAbstract);
        return article;
    }
}
