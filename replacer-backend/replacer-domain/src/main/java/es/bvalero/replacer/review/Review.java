package es.bvalero.replacer.review;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaSection;
import java.util.Collection;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Review of a page */
@Value(staticConstructor = "of")
class Review {

    @NonNull
    WikipediaPage page;

    @Nullable
    WikipediaSection section;

    @NonNull
    Collection<Replacement> replacements;

    @Nullable
    Integer numPending;
}