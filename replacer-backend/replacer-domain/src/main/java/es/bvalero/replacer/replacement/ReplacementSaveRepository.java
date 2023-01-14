package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import java.util.Collection;
import org.springframework.lang.Nullable;

interface ReplacementSaveRepository {
    /** Add a collection of page replacements assuming the related pages already exist */
    void add(Collection<IndexedReplacement> replacements);

    /** Update a collection of page replacements */
    void update(Collection<IndexedReplacement> replacements);

    /** Delete a collection of page replacements */
    void remove(Collection<IndexedReplacement> replacements);

    /** Update the reviewer of all the replacements of the given type to review */
    void updateReviewerByType(WikipediaLanguage lang, ReplacementType type, String reviewer);

    /** Update the reviewer of all the replacements of the given page and (optionally) type to review */
    void updateReviewerByPageAndType(PageKey pageKey, @Nullable ReplacementType type, String reviewer);

    /**
     * Update the reviewer of a collection of replacements.
     * Only the replacements to review are updated.
     * The replacements to update are not identified by ID, but by page-key, type and start.
     */
    void updateReviewer(Collection<IndexedReplacement> replacements);

    /** Delete all the replacements to review by type */
    void removeByType(WikipediaLanguage lang, ReplacementType type);
}
