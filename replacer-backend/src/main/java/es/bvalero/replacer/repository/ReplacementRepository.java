package es.bvalero.replacer.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;

public interface ReplacementRepository {
    String REVIEWER_SYSTEM = "system";

    /** Insert a collection of replacements assuming the related pages already exist */
    void insertReplacements(Collection<ReplacementModel> replacements);

    /** Update a collection of replacements */
    void updateReplacements(Collection<ReplacementModel> replacements);

    /** Delete a collection of replacements */
    void deleteReplacements(Collection<ReplacementModel> replacements);

    void reviewAsSystemByType(WikipediaLanguage lang, String type, String subtype);
}
