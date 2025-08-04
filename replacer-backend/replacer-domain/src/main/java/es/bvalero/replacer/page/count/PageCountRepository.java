package es.bvalero.replacer.page.count;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.StandardType;
import java.util.Collection;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.springframework.lang.Nullable;

@SecondaryPort
public interface PageCountRepository {
    /** Count the number of pages to review grouped by replacement type */
    Collection<ResultCount<StandardType>> countNotReviewedGroupedByType(WikipediaLanguage lang);

    /** Count the number of pages to review (optionally by type) */
    int countNotReviewedByType(WikipediaLanguage lang, @Nullable StandardType type);
}
