package es.bvalero.replacer.article;

import java.time.LocalDate;

public class ArticleTimestamp {

    private final int articleId;
    private final LocalDate lastUpdate;

    public ArticleTimestamp(int articleId, LocalDate lastUpdate) {
        this.articleId = articleId;
        this.lastUpdate = lastUpdate;
    }

    public int getArticleId() {
        return articleId;
    }

    public LocalDate getLastUpdate() {
        return lastUpdate;
    }
}
