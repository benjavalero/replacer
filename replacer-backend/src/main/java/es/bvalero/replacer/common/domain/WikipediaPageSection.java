package es.bvalero.replacer.common.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Domain entity. It represents a section of a page in Wikipedia.
 *
 * It contains the same attributes as the page, plus an attribute with the section attributes.
 * This section attribute can be null, meaning that the page section is actually the parent page.
 * If the section attribute exists, the content is the one of the section.
 */
@Value
@Builder
public class WikipediaPageSection {

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

    @Nullable
    WikipediaSection section;

    public static WikipediaPageSection of(WikipediaPage page, @Nullable WikipediaSection section) {
        return WikipediaPageSection
            .builder()
            .id(page.getId())
            .namespace(page.getNamespace())
            .title(page.getTitle())
            .content(page.getContent())
            .lastUpdate(page.getLastUpdate())
            .queryTimestamp(page.getQueryTimestamp())
            .section(section)
            .build();
    }
}
