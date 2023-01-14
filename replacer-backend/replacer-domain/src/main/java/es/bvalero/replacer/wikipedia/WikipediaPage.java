package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.index.IndexablePage;
import es.bvalero.replacer.page.PageKey;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

/** Page in Wikipedia */
@Value
@Builder
public class WikipediaPage implements IndexablePage {

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

    @NonNull
    LocalDateTime lastUpdate; // Store time (and not only date) in case it is needed in the future

    @NonNull
    @ToString.Exclude
    LocalDateTime queryTimestamp; // Store the timestamp when the page was queried

    @ToString.Exclude
    boolean redirect; // If the page is considered a redirection page

    // Lombok trick to print only a fragment of the page content
    @ToString.Include
    private String shortContent() {
        return StringUtils.abbreviate(getContent(), SHORT_CONTENT_LENGTH);
    }

    @TestOnly
    public int getPageId() {
        return getPageKey().getPageId();
    }
}
