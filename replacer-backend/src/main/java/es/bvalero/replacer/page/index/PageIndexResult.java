package es.bvalero.replacer.page.index;

import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Sub-domain object representing the result of indexing a page or several pages.
 * We implement it as a mutable object (we can add items to the collections)
 * and with builder pattern for simplicity.
 */
@Getter(AccessLevel.PACKAGE)
@EqualsAndHashCode
@Builder
final class PageIndexResult {

    /* Changes to be applied in the database */

    // Pages to be created along with the related replacements
    @Builder.Default
    Set<IndexablePage> createPages = new HashSet<>();

    // The attributes of a page may vary with time. In particular, we are interested in update the titles.
    @Builder.Default
    Set<IndexablePage> updatePages = new HashSet<>();

    @Builder.Default
    Set<IndexablePage> deletePages = new HashSet<>();

    @Builder.Default
    Set<IndexableReplacement> createReplacements = new HashSet<>();

    // The attributes of a replacement may vary, e.g. the last update date, or the context/position.
    @Builder.Default
    Set<IndexableReplacement> updateReplacements = new HashSet<>();

    @Builder.Default
    Set<IndexableReplacement> deleteReplacements = new HashSet<>();

    int size() {
        return (
            createPages.size() +
            updatePages.size() +
            deletePages.size() +
            createReplacements.size() +
            updateReplacements.size() +
            deleteReplacements.size()
        );
    }

    boolean isNotEmpty() {
        return this.size() > 0;
    }

    void add(PageIndexResult pageIndexResult) {
        this.createPages.addAll(pageIndexResult.getCreatePages());
        this.updatePages.addAll(pageIndexResult.getUpdatePages());
        this.deletePages.addAll(pageIndexResult.getDeletePages());
        this.createReplacements.addAll(pageIndexResult.getCreateReplacements());
        this.updateReplacements.addAll(pageIndexResult.getUpdateReplacements());
        this.deleteReplacements.addAll(pageIndexResult.getDeleteReplacements());
    }

    static PageIndexResult ofEmpty() {
        return PageIndexResult.builder().build();
    }

    void clear() {
        this.createPages.clear();
        this.updatePages.clear();
        this.deletePages.clear();
        this.createReplacements.clear();
        this.updateReplacements.clear();
        this.deleteReplacements.clear();
    }
}
