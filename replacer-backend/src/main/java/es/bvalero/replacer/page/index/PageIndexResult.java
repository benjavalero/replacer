package es.bvalero.replacer.page.index;

import es.bvalero.replacer.page.repository.IndexablePage;
import es.bvalero.replacer.page.repository.IndexableReplacement;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

/**
 * Sub-domain object representing the result of indexing a page or several pages
 * We implement it as a mutable object (we can add items to the collections)
 * and with builder pattern for simplicity.
 */
@Getter(AccessLevel.PACKAGE)
@EqualsAndHashCode
@Builder
public final class PageIndexResult {

    // Pages to be created along with the related replacements
    @Builder.Default
    Set<IndexablePage> createPages = new HashSet<>();

    // The attributes of a page may vary with time. In particular, we are interested in update the titles.
    @Builder.Default
    Set<IndexablePage> updatePages = new HashSet<>();

    @Builder.Default
    Set<IndexableReplacement> createReplacements = new HashSet<>();

    // The attributes of a replacement may vary, e.g. the last update date, or the context/position.
    @Builder.Default
    Set<IndexableReplacement> updateReplacements = new HashSet<>();

    @Builder.Default
    Set<IndexableReplacement> deleteReplacements = new HashSet<>();

    public int size() {
        return (
            createPages.size() +
            updatePages.size() +
            createReplacements.size() +
            updateReplacements.size() +
            deleteReplacements.size()
        );
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    void add(PageIndexResult pageIndexResult) {
        this.createPages.addAll(pageIndexResult.getCreatePages());
        this.updatePages.addAll(pageIndexResult.getUpdatePages());
        this.createReplacements.addAll(pageIndexResult.getCreateReplacements());
        this.updateReplacements.addAll(pageIndexResult.getUpdateReplacements());
        this.deleteReplacements.addAll(pageIndexResult.getDeleteReplacements());
    }

    public static PageIndexResult ofEmpty() {
        return PageIndexResult.builder().build();
    }

    void clear() {
        this.createPages.clear();
        this.updatePages.clear();
        this.createReplacements.clear();
        this.updateReplacements.clear();
        this.deleteReplacements.clear();
    }
}
