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

    void remove(WikipediaLanguage lang, StandardType type);

    void increment(WikipediaLanguage lang, StandardType type);

    void decrement(WikipediaLanguage lang, StandardType type);
}
