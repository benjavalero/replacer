package es.bvalero.replacer.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;

public interface ReplacementRepository {
    String REVIEWER_SYSTEM = "system";

    /** Add a collection of replacements assuming the related pages already exist */
    void addReplacements(Collection<ReplacementModel> replacements);

    /** Update a collection of replacements */
    void updateReplacements(Collection<ReplacementModel> replacements);

    /** Delete a collection of replacements */
    void removeReplacements(Collection<ReplacementModel> replacements);

    /** Find a random replacement to review */
    int findReplacementToReview(WikipediaLanguage lang, int chunkSize);
}
