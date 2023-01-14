package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;

interface PageCountRepository {
    /** Count the number of pages to review by replacement type */
    Collection<ResultCount<ReplacementType>> countPagesNotReviewedByType(WikipediaLanguage lang);
}
