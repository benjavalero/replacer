package es.bvalero.replacer.page.index;

import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Setter;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Validates if a Wikipedia page is indexable according to its namespace.
 * Note it is a Component because the indexable namespaces are configurable,
 * and thus they are not known until the application is running.
 */
@Component
class PageIndexValidator {

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.indexable.namespaces}")
    private Set<Integer> indexableNamespaces;

    private Set<WikipediaNamespace> indexableWikipediaNamespaces;

    @PostConstruct
    public void init() {
        this.indexableWikipediaNamespaces = this.indexableNamespaces.stream()
            .map(WikipediaNamespace::valueOf)
            .collect(Collectors.toUnmodifiableSet());
    }

    boolean isPageIndexableByNamespace(IndexablePage page) {
        return this.indexableWikipediaNamespaces.contains(page.getNamespace());
    }

    boolean isIndexableByTimestamp(IndexablePage page, @Nullable IndexedPage dbPage) {
        // If page modified in dump equals to the last indexing, always reindex.
        // If page modified in dump after last indexing, always reindex.
        // If page modified in dump before last indexing, do not index.
        // So we return page.date >= dbPage.date
        return dbPage == null || !page.getLastUpdate().isBefore(dbPage.getLastUpdate());
    }
}
