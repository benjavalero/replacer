package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

/** Repository to retrieve the replacement counts for statistics */
@SecondaryPort
interface ReplacementCountRepository {
    /** Count the number of reviewed replacements including the custom ones */
    int countReviewed(WikipediaLanguage lang);

    /** Count the number of replacements to review */
    int countNotReviewed(WikipediaLanguage lang);

    /** Count the number of reviewed replacements, including the custom ones, grouped by reviewer */
    Collection<ResultCount<String>> countReviewedGroupedByReviewer(WikipediaLanguage lang);

    /** Count the number of replacements to review grouped by page in descending order by count */
    Collection<ResultCount<PageTitle>> countNotReviewedGroupedByPage(WikipediaLanguage lang, int numResults);
}
