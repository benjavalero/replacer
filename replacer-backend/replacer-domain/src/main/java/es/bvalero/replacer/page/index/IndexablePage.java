package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import java.time.LocalDate;
import java.util.Objects;
import lombok.*;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

@Value
@Builder
public class IndexablePage {

    @NonNull
    PageKey pageKey;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    int namespace;

    WikipediaNamespace getNamespace() {
        return WikipediaNamespace.valueOf(this.namespace);
    }

    @NonNull
    String title;

    @NonNull
    @ToString.Exclude
    @With(onMethod_ = @TestOnly)
    String content;

    @Getter(AccessLevel.NONE)
    @NonNull
    String lastUpdate;

    @ToString.Exclude
    boolean redirect;

    LocalDate getLastUpdate() {
        return WikipediaTimestamp.of(this.lastUpdate).toLocalDate();
    }

    FinderPage toFinderPage() {
        return FinderPage.of(getPageKey(), Objects.requireNonNull(getTitle()), Objects.requireNonNull(getContent()));
    }

    public static IndexablePage of(WikipediaPage page) {
        return IndexablePage.builder()
            .pageKey(page.getPageKey())
            .namespace(page.getNamespace().getValue())
            .title(page.getTitle())
            .content(page.getContent())
            .lastUpdate(page.getLastUpdate().toString())
            .build();
    }
}
