package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.PageKey;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

/**
 * A Wikipedia page is a page retrieved from Wikipedia, from any language or namespace.
 * It contains the most important properties, in particular the text content.
 * It is actually a snapshot, as the page content can still be modified later by any Wikipedia user,
 * so we can define this class as immutable.
 */
@Value
@Builder
public class WikipediaPage {

    private static final int SHORT_CONTENT_LENGTH = 50;

    @NonNull
    PageKey pageKey;

    @NonNull
    @ToString.Exclude
    WikipediaNamespace namespace;

    @NonNull
    String title;

    @NonNull
    @ToString.Exclude
    String content;

    /* Store time (and not only date) in case it is needed in the future */
    @NonNull
    WikipediaTimestamp lastUpdate;

    /* If the page is now missing */
    @ToString.Exclude
    @Builder.Default
    boolean missing = false;

    /* If the page is considered a redirection page */
    @ToString.Exclude
    boolean redirect;

    /* If the page is protected for librarians */
    @ToString.Exclude
    @Accessors(fluent = true)
    @Builder.Default
    boolean isProtected = false;

    /* Store the timestamp when the page was queried */
    @NonNull
    @ToString.Exclude
    WikipediaTimestamp queryTimestamp;

    // Lombok trick to print only a fragment of the page content
    @ToString.Include
    private String getAbbreviatedContent() {
        return StringUtils.abbreviate(getContent(), SHORT_CONTENT_LENGTH);
    }

    // Shorthand to get the page ID
    @TestOnly
    public int getPageId() {
        return getPageKey().getPageId();
    }
}
