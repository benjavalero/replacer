package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.replacement.IndexedReplacement;
import java.util.Collection;
import java.util.HashSet;
import lombok.Value;

/** Result of comparing an indexable page with an already indexed one */
@Value(staticConstructor = "of")
class PageComparatorResult {

    WikipediaLanguage lang;

    /* Changes to be applied in the database */

    // Pages to be created. The related replacements must be added apart.
    Collection<IndexedPage> addPages = new HashSet<>();

    // The attributes of a page may vary with time. In particular, we are interested in update the titles.
    Collection<IndexedPage> updatePages = new HashSet<>();

    Collection<IndexedReplacement> addReplacements = new HashSet<>();

    // The attributes of a replacement may vary, e.g. the last update date, or the context/position.
    Collection<IndexedReplacement> updateReplacements = new HashSet<>();

    Collection<IndexedReplacement> removeReplacements = new HashSet<>();

    Collection<ReplacementType> addReplacementTypes = new HashSet<>();

    Collection<ReplacementType> removeReplacementTypes = new HashSet<>();

    /* The actual replacements to be reviewed after indexing, as some may have been discarded as already reviewed. */

    Collection<Replacement> replacementsToReview = new HashSet<>();

    void addPage(IndexedPage page) {
        this.addPages.add(page);
    }

    void updatePage(IndexedPage page) {
        this.updatePages.add(page);
    }

    void addReplacement(ComparableReplacement replacement) {
        this.addReplacements.add(replacement.toDomain());
    }

    void updateReplacement(ComparableReplacement replacement) {
        this.updateReplacements.add(replacement.toDomain());
    }

    void removeReplacement(ComparableReplacement replacement) {
        this.removeReplacements.add(replacement.toDomain());
    }

    void addReplacementsToReview(Collection<Replacement> replacements) {
        this.replacementsToReview.addAll(replacements);
    }

    void addReplacementTypes(Collection<ReplacementType> replacementTypes) {
        this.addReplacementTypes.addAll(replacementTypes);
    }

    void removeReplacementTypes(Collection<ReplacementType> replacementTypes) {
        this.removeReplacementTypes.addAll(replacementTypes);
    }

    int size() {
        return (
            this.addPages.size() +
            this.updatePages.size() +
            this.addReplacements.size() +
            this.updateReplacements.size() +
            this.removeReplacements.size()
        );
    }

    boolean isEmpty() {
        return size() == 0;
    }
}
