package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.Replacement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

/**
 * Sub-domain object representing the result of indexing a page or several pages.
 * We implement it as a mutable object (we can add items to the collections)
 * and with builder pattern for simplicity.
 */
@Getter(AccessLevel.PACKAGE)
@EqualsAndHashCode
@Builder(access = AccessLevel.PACKAGE)
public final class PageIndexResult {

    /* Resulting status of the page indexing */

    @NonNull
    @Getter(AccessLevel.PUBLIC)
    @Builder.Default
    PageIndexStatus status = PageIndexStatus.PAGE_NOT_INDEXED;

    /* Replacements resolved from the page content */
    @NonNull
    @With
    @Getter(AccessLevel.PUBLIC)
    @Builder.Default
    Collection<Replacement> replacements = Collections.emptyList();

    /* Changes to be applied in the database */

    // Pages to be created. The related replacements must be added apart.
    @Builder.Default
    Set<IndexablePage> addPages = new HashSet<>();

    // The attributes of a page may vary with time. In particular, we are interested in update the titles.
    @Builder.Default
    Set<IndexablePage> updatePages = new HashSet<>();

    @Builder.Default
    Set<IndexablePage> removePages = new HashSet<>();

    @Builder.Default
    Set<IndexableReplacement> addReplacements = new HashSet<>();

    // The attributes of a replacement may vary, e.g. the last update date, or the context/position.
    @Builder.Default
    Set<IndexableReplacement> updateReplacements = new HashSet<>();

    @Builder.Default
    Set<IndexableReplacement> removeReplacements = new HashSet<>();

    int size() {
        return (
            addPages.size() +
            updatePages.size() +
            removePages.size() +
            addReplacements.size() +
            updateReplacements.size() +
            removeReplacements.size()
        );
    }

    boolean isNotEmpty() {
        return this.size() > 0;
    }

    void add(PageIndexResult pageIndexResult) {
        this.addPages.addAll(pageIndexResult.getAddPages());
        this.updatePages.addAll(pageIndexResult.getUpdatePages());
        this.removePages.addAll(pageIndexResult.getRemovePages());
        this.addReplacements.addAll(pageIndexResult.getAddReplacements());
        this.updateReplacements.addAll(pageIndexResult.getUpdateReplacements());
        this.removeReplacements.addAll(pageIndexResult.getRemoveReplacements());

        if (isNotEmpty()) {
            this.status = PageIndexStatus.PAGE_INDEXED;
        }
    }

    static PageIndexResult ofEmpty() {
        return PageIndexResult.builder().build();
    }

    @TestOnly
    public static PageIndexResult ofEmpty(PageIndexStatus status, Collection<Replacement> replacements) {
        return PageIndexResult.builder().status(status).replacements(replacements).build();
    }

    void clear() {
        this.addPages.clear();
        this.updatePages.clear();
        this.removePages.clear();
        this.addReplacements.clear();
        this.updateReplacements.clear();
        this.removeReplacements.clear();

        this.status = PageIndexStatus.PAGE_NOT_INDEXED;
    }
}
