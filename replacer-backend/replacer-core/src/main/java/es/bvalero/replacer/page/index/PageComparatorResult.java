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
    Collection<IndexedPage> pagesToCreate = new HashSet<>();

    // The attributes of a page may vary with time. In particular, we are interested in update the titles.
    Collection<IndexedPage> pagesToUpdate = new HashSet<>();

    Collection<IndexedReplacement> replacementsToCreate = new HashSet<>();

    // The attributes of a replacement may vary, e.g. the last update date, or the context/position.
    Collection<IndexedReplacement> replacementsToUpdate = new HashSet<>();

    Collection<IndexedReplacement> replacementsToDelete = new HashSet<>();

    Collection<ReplacementType> replacementTypesToCreate = new HashSet<>();

    Collection<ReplacementType> replacementTypesToDelete = new HashSet<>();

    /* The actual replacements to be reviewed after indexing, as some may have been discarded as already reviewed. */

    Collection<Replacement> replacementsToReview = new HashSet<>();

    void addPageToCreate(IndexedPage page) {
        this.pagesToCreate.add(page);
    }

    void addPageToUpdate(IndexedPage page) {
        this.pagesToUpdate.add(page);
    }

    void addReplacementToCreate(ComparableReplacement replacement) {
        this.replacementsToCreate.add(replacement.toDomain());
    }

    void addReplacementToUpdate(ComparableReplacement replacement) {
        this.replacementsToUpdate.add(replacement.toDomain());
    }

    void addReplacementToDelete(ComparableReplacement replacement) {
        this.replacementsToDelete.add(replacement.toDomain());
    }

    void addReplacementsToReview(Collection<Replacement> replacements) {
        this.replacementsToReview.addAll(replacements);
    }

    void addReplacementTypesToCreate(Collection<ReplacementType> replacementTypes) {
        this.replacementTypesToCreate.addAll(replacementTypes);
    }

    void addReplacementTypesToDelete(Collection<ReplacementType> replacementTypes) {
        this.replacementTypesToDelete.addAll(replacementTypes);
    }

    int size() {
        return (
            this.pagesToCreate.size() +
            this.pagesToUpdate.size() +
            this.replacementsToCreate.size() +
            this.replacementsToUpdate.size() +
            this.replacementsToDelete.size()
        );
    }

    boolean isEmpty() {
        return size() == 0;
    }
}
