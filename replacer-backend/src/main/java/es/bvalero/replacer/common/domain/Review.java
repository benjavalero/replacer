package es.bvalero.replacer.common.domain;

import java.util.Collection;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Review of a page */
@Value(staticConstructor = "of")
public class Review {

    @NonNull
    WikipediaPage page;

    @Nullable
    WikipediaSection section;

    @NonNull
    Collection<Replacement> replacements;

    @Nullable
    Integer numPending;
}
