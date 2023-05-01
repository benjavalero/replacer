package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.page.PageKey;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** A group with all the parameters needed to save a page in Wikipedia. */
@Value
@Builder
public class WikipediaPageSave {

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
