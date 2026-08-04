package es.bvalero.replacer.review;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaSection;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Review of a page */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Value
class Review {

    @NonNull
    WikipediaPage page;

    @Nullable
    WikipediaSection section;

    @NonNull
    Collection<Replacement> replacements;

    @Nullable
    Integer numPending;

    public static Review of(
        WikipediaPage page,
        @Nullable WikipediaSection section,
        Collection<Replacement> replacements,
        @Nullable Integer numPending
    ) {
        // Validate replacement positions
        replacements.forEach(replacement -> replacement.validate(page.getContent()));

        return new Review(page, section, replacements, numPending);
    }
}
