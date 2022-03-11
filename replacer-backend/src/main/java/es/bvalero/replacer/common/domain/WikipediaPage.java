package es.bvalero.replacer.common.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

/** Page in Wikipedia */
@NonFinal // So it can be mocked
@Value
@Builder
public class WikipediaPage {

    private static final int SHORT_CONTENT_LENGTH = 50;

    @NonNull
    WikipediaPageId id;

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
    @Builder.Default
    LocalDateTime queryTimestamp = LocalDateTime.now(); // Store the timestamp when the page was queried

    @ToString.Exclude
    @Builder.Default
    boolean redirect = false; // If the page is considered a redirection page

    // Lombok trick to print only a fragment of the page content
    @ToString.Include
    private String shortContent() {
        return StringUtils.abbreviate(this.getContent(), SHORT_CONTENT_LENGTH);
    }

    @TestOnly
    public static WikipediaPage of(String content) {
        return of(WikipediaLanguage.getDefault(), content, "");
    }

    @TestOnly
    public static WikipediaPage of(WikipediaLanguage lang, String content, String title) {
        return WikipediaPage
            .builder()
            .id(WikipediaPageId.of(lang, 1))
            .namespace(WikipediaNamespace.getDefault())
            .title(title)
            .content(content)
            .lastUpdate(LocalDateTime.now())
            .build();
    }
}
