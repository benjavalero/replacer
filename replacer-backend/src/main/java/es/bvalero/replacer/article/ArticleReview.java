package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.ArticleReplacement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Domain class of an article to review to be used in the front-end.
 */
public final class ArticleReview {

    private final Integer articleId;
    private final String title;
    private final String content;
    private final List<ArticleReplacement> replacements;
    private final String lastUpdate;
    private final String currentTimestamp;

    private ArticleReview(Integer articleId, String title, String content, List<ArticleReplacement> replacements,
                          String lastUpdate, String currentTimestamp) {
        this.articleId = articleId;
        this.title = title;
        this.content = content;
        this.replacements = replacements;
        this.lastUpdate = lastUpdate;
        this.currentTimestamp = currentTimestamp;
    }

    public static ArticleReview.ArticleReviewBuilder builder() {
        return new ArticleReview.ArticleReviewBuilder();
    }

    @SuppressWarnings("unused")
    public Integer getArticleId() {
        return articleId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    @SuppressWarnings("unused")
    public String getLastUpdate() {
        return lastUpdate;
    }

    @SuppressWarnings("unused")
    public String getCurrentTimestamp() {
        return currentTimestamp;
    }

    public List<ArticleReplacement> getReplacements() {
        return Collections.unmodifiableList(replacements);
    }

    @Override
    public String toString() {
        return "ArticleReview{" +
                "articleId=" + articleId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", replacements=" + replacements +
                ", lastUpdate='" + lastUpdate + '\'' +
                ", currentTimestamp='" + currentTimestamp + '\'' +
                '}';
    }

    static class ArticleReviewBuilder {
        private final List<ArticleReplacement> replacements = new ArrayList<>(100);
        private Integer articleId;
        private String title;
        private String content;
        private String lastUpdate;
        private String currentTimestamp;

        ArticleReview.ArticleReviewBuilder setArticleId(Integer articleId) {
            this.articleId = articleId;
            return this;
        }

        ArticleReview.ArticleReviewBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        ArticleReview.ArticleReviewBuilder setContent(String content) {
            this.content = content;
            return this;
        }

        ArticleReview.ArticleReviewBuilder setReplacements(Collection<ArticleReplacement> replacements) {
            this.replacements.clear();
            this.replacements.addAll(replacements);
            return this;
        }

        ArticleReview.ArticleReviewBuilder setLastUpdate(String lastUpdate) {
            this.lastUpdate = lastUpdate;
            return this;
        }

        ArticleReview.ArticleReviewBuilder setCurrentTimestamp(String currentTimestamp) {
            this.currentTimestamp = currentTimestamp;
            return this;
        }

        ArticleReview build() {
            return new ArticleReview(articleId, title, content, replacements, lastUpdate, currentTimestamp);
        }
    }

}
