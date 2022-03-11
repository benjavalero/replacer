package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaSection;
import java.util.Collection;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Page to review in the front-end */
@Value(staticConstructor = "of")
class PageReview {

    @NonNull
    WikipediaPage page;

    @Nullable
    WikipediaSection section;

    @NonNull
    Collection<Replacement> replacements;

    @Nullable
    Integer numPending;
}
