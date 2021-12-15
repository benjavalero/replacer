package es.bvalero.replacer.repository;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import org.springframework.lang.Nullable;

public interface ReplacementRepository {
    String REVIEWER_SYSTEM = "system";

    /** Add a collection of replacements assuming the related pages already exist */
    void addReplacements(Collection<ReplacementModel> replacements);

    /** Update a collection of replacements */
    void updateReplacements(Collection<ReplacementModel> replacements);

    /** Delete a collection of replacements */
    void removeReplacements(Collection<ReplacementModel> replacements);

    /** Count the number of replacements to review by type */
    Collection<ResultCount<ReplacementType>> countReplacementsByType(WikipediaLanguage lang);

    /** Update the reviewer of all the replacements of the given type to review */
    void updateReviewerByType(WikipediaLanguage lang, String type, String subtype, String reviewer);

    /** Update the reviewer of all the replacements of the given page and (optionally) type to review */
    void updateReviewerByPageAndType(
        WikipediaLanguage lang,
        int pageId,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    );

    /** Delete all the replacements to review by the given types */
    void removeReplacementsByType(WikipediaLanguage lang, String type, Collection<String> subtypes);

    /** Find a random replacement to review */
    long findReplacementToReview(WikipediaLanguage lang, long chunkSize);
}
