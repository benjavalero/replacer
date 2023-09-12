package es.bvalero.replacer.dump;

import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.index.IndexablePage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.springframework.lang.NonNull;

/**
 * A dump page is a page retrieved from a Wikipedia dump, from any language or namespace.
 * It contains the most important properties, in particular the text content.
 * It is obviously a snapshot, as the page content may have been modified later by any Wikipedia user,
 * so we can define this class as immutable.
 */
@Value
@Builder
class DumpPage implements IndexablePage {

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

    // Lombok trick to print only a fragment of the page content
    @ToString.Include
    private String shortContent() {
        return getAbbreviatedContent();
    }
}
