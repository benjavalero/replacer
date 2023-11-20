package es.bvalero.replacer.page.count;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import org.springframework.lang.Nullable;

public interface PageCountRepository {
    /** Count the number of pages to review grouped by replacement type */
    Collection<ResultCount<StandardType>> countNotReviewedGroupedByType(WikipediaLanguage lang);

    /** Count the number of pages to review (optionally by type) */
    int countNotReviewedByType(WikipediaLanguage lang, @Nullable StandardType type);

    /** Remove the page count from the cache for the given type */
    void removePageCountByType(WikipediaLanguage lang, StandardType type);

    /** Increment the page count in the cache for the given type */
    void incrementPageCountByType(WikipediaLanguage lang, StandardType type);

    /** Decrement the page count in the cache for the given type */
    void decrementPageCountByType(WikipediaLanguage lang, StandardType type);
}
