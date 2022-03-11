package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.Replacement;
import java.util.Collection;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

/** Result of indexing a page or several pages */
@Value
@Builder(toBuilder = true, access = AccessLevel.PACKAGE)
public class PageIndexResult {

    /* Resulting status of the page indexing */

    @NonNull
    @Builder.Default
    PageIndexStatus status = PageIndexStatus.PAGE_NOT_INDEXED;

    /* Replacements resolved from the page content */
    @Singular
    Collection<Replacement> replacements;

    /* Changes to be applied in the database */

    // Pages to be created. The related replacements must be added apart.
    @Singular
    Set<IndexablePage> addPages;

    // The attributes of a page may vary with time. In particular, we are interested in update the titles.
    @Singular
    Set<IndexablePage> updatePages;

    @Singular
    Set<IndexableReplacement> addReplacements;

    // The attributes of a replacement may vary, e.g. the last update date, or the context/position.
    @Singular
    Set<IndexableReplacement> updateReplacements;

    @Singular
    Set<IndexableReplacement> removeReplacements;

    int size() {
        return (
            addPages.size() +
            updatePages.size() +
            addReplacements.size() +
            updateReplacements.size() +
            removeReplacements.size()
        );
    }

    private boolean isNotEmpty() {
        return this.size() > 0;
    }

    PageIndexResult add(PageIndexResult pageIndexResult) {
        PageIndexResult merged =
            this.toBuilder()
                .addPages(pageIndexResult.getAddPages())
                .updatePages(pageIndexResult.getUpdatePages())
                .addReplacements(pageIndexResult.getAddReplacements())
                .updateReplacements(pageIndexResult.getUpdateReplacements())
                .removeReplacements(pageIndexResult.getRemoveReplacements())
                .build();
        if (pageIndexResult.isNotEmpty()) {
            merged = merged.toBuilder().status(PageIndexStatus.PAGE_INDEXED).build();
        }
        return merged;
    }

    static PageIndexResult ofEmpty() {
        return PageIndexResult.builder().build();
    }

    @TestOnly
    public static PageIndexResult ofEmpty(PageIndexStatus status, Collection<Replacement> replacements) {
        return PageIndexResult.builder().status(status).replacements(replacements).build();
    }
}
