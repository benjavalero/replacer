package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.index.IndexablePage;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
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
public class WikipediaPage implements IndexablePage {

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

    @NonNull
    WikipediaTimestamp lastUpdate;

    @ToString.Exclude
    boolean redirect;

    /* Store the timestamp when the page was queried */
    @NonNull
    @ToString.Exclude
    WikipediaTimestamp queryTimestamp;

    // Lombok trick to print only a fragment of the page content
    @ToString.Include
    private String shortContent() {
        return getAbbreviatedContent();
    }

    // Shorthand to get the page ID
    @TestOnly
    public int getPageId() {
        return getPageKey().getPageId();
    }
}
