package es.bvalero.replacer.dump;

import es.bvalero.replacer.index.IndexablePage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

/** Page in a Wikipedia dump */
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
    WikipediaTimestamp lastUpdate; // Store time (and not only date) in case it is needed in the future

    @ToString.Exclude
    boolean redirect; // If the page is considered a redirection page

    // Lombok trick to print only a fragment of the page content
    @ToString.Include
    private String shortContent() {
        return StringUtils.abbreviate(getContent(), SHORT_CONTENT_LENGTH);
    }
}
