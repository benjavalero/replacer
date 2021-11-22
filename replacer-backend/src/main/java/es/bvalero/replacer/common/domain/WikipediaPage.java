package es.bvalero.replacer.common.domain;

import es.bvalero.replacer.wikipedia.WikipediaSection;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.experimental.NonFinal;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Domain entity. It represents a page in Wikipedia: title, content, etc.
 *
 * Once instantiated it becomes immutable. Even when all attributes are mandatory,
 * we implement a builder factory not to have a constructor with too many arguments.
 *
 * Finally, we make the class non-final, so it can be inherited.
 */
@NonFinal
@Value
@Builder
public class WikipediaPage {

    @NonNull
    WikipediaPageId id;

    @NonNull
    WikipediaNamespace namespace;

    @NonNull
    String title;

    @NonNull
    String content;

    @NonNull
    LocalDateTime lastUpdate; // Store time (and not only date) in case it is needed in the future

    @NonNull
    @Builder.Default
    LocalDateTime queryTimestamp = LocalDateTime.now(); // Store the timestamp when the page was queried

    // It may also represent a specific section of a page, as they are the same thing for Wikipedia API.
    // TODO: Create a sub-class containing the section.
    @With
    @Nullable
    WikipediaSection section; // Defined in case it is a section and null if it is the whole page
}
