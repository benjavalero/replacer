package es.bvalero.replacer.page;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.List;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.Nullable;

/**
 * Domain class of a page to review to be used in the front-end.
 */
@Value
@Builder
class PageReview {

    int id;
    WikipediaLanguage lang;
    String title;
    String content;

    @Nullable
    @With(AccessLevel.PACKAGE)
    Integer section;

    @Nullable
    String anchor;

    String queryTimestamp;
    List<PageReplacement> replacements;
    long numPending;

    static PageReview of(WikipediaPage page, List<PageReplacement> replacements, long numPending) {
        return PageReview
            .builder()
            .id(page.getId())
            .lang(page.getLang())
            .title(page.getTitle())
            .content(page.getContent())
            .section(page.getSection())
            .anchor(page.getAnchor())
            .queryTimestamp(page.getQueryTimestamp())
            .replacements(replacements)
            .numPending(numPending)
            .build();
    }

    @TestOnly
    static PageReview ofEmpty() {
        return PageReview.builder().build();
    }

    @Override
    public String toString() {
        return (
            "PageReview(id=" +
            this.getId() +
            ", lang=" +
            this.getLang() +
            ", title=" +
            this.getTitle() +
            ", content=" +
            StringUtils.abbreviate(this.getContent(), PageController.CONTENT_SIZE) +
            ", section=" +
            this.getSection() +
            ", anchor=" +
            this.getAnchor() +
            ", queryTimestamp=" +
            this.getQueryTimestamp() +
            ", replacements=" +
            this.getReplacements() +
            ", numPending=" +
            this.getNumPending() +
            ")"
        );
    }
}
