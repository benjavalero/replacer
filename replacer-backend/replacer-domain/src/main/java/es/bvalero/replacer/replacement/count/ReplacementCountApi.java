package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.PageTitle;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

/** Service to retrieve the replacement counts for statistics */
@PrimaryPort
interface ReplacementCountApi {
    /** Count the number of reviewed replacements including the custom ones */
    int countReviewed(WikipediaLanguage lang);

    /** Count the number of replacements to review */
    int countNotReviewed(WikipediaLanguage lang);

    /**
     * Count the number of reviewed replacements, including the custom ones,
     * grouped by reviewer in descending order by count.
     */
    Collection<ResultCount<String>> countReviewedGroupedByReviewer(WikipediaLanguage lang);

    /** Count the number of replacements to review grouped by page in descending order by count */
    Collection<ResultCount<PageTitle>> countNotReviewedGroupedByPage(WikipediaLanguage lang);
}
