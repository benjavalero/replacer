package es.bvalero.replacer.dump;

import es.bvalero.replacer.index.IndexablePage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;

/** Page in a Wikipedia dump */
@Value
@Builder
class DumpPage implements IndexablePage {

    @NonNull
    PageKey pageKey;

    @NonNull
    WikipediaNamespace namespace;

    @NonNull
    String title;

    @NonNull
    String content;

    @NonNull
    LocalDateTime lastUpdate; // Store time (and not only date) in case it is needed in the future

    boolean redirect; // If the page is considered a redirection page
}
