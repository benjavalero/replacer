package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.PageKey;
import lombok.Builder;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** A group with all the parameters needed to save a page in Wikipedia. */
@Value
@Builder
public class WikipediaPageSaveCommand {

    @NonNull
    PageKey pageKey;

    @Nullable
    Integer sectionId;

    @NonNull
    String content;

    @NonNull
    String editSummary;

    @NonNull
    WikipediaTimestamp queryTimestamp; // To check edit conflicts
}
