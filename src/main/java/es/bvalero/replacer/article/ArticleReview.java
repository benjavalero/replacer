package es.bvalero.replacer.article;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Domain class of an article to review to be used in the front-end.
 */
@SuppressWarnings("unused")
public final class ArticleReview {

    private final String title;
    private final String content;
    private final List<ArticleReplacement> replacements;
    private final boolean trimText;

    private ArticleReview(String title, String content, List<ArticleReplacement> replacements, boolean trimText) {
        this.title = title;
        this.content = content;
        this.replacements = replacements;
        this.trimText = trimText;
    }

    public static ArticleReview.ArticleReviewBuilder builder() {
        return new ArticleReview.ArticleReviewBuilder();
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public List<ArticleReplacement> getReplacements() {
        return Collections.unmodifiableList(replacements);
    }

    public boolean isTrimText() {
        return trimText;
    }

    static class ArticleReviewBuilder {
        private final List<ArticleReplacement> replacements = new ArrayList<>(100);
        private String title;
        private String content;
        private boolean trimText;

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

        ArticleReview.ArticleReviewBuilder setTrimText(boolean trimText) {
            this.trimText = trimText;
            return this;
        }

        ArticleReview build() {
            return new ArticleReview(title, content, replacements, trimText);
        }
    }

}
