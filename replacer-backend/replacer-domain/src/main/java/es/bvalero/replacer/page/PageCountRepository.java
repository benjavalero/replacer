package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;

interface PageCountRepository {
    /**
     * Count the number of pages to review by replacement type
     */
    Collection<ResultCount<StandardType>> countPagesNotReviewedByType(WikipediaLanguage lang);

    /** Count the number of pages to review (optionally by type) */
    int countNotReviewedByType(WikipediaLanguage lang, ReplacementType type);
}
