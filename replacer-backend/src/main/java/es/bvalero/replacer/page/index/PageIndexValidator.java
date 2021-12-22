package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Validates if a Wikipedia page is indexable according to its namespace.
 *
 * Note it is a Component because the indexable namespaces are configurable,
 * and thus they are not known until the application is running.
 */
@Component
class PageIndexValidator {

    @Value("${replacer.indexable.namespaces}")
    private Set<Integer> indexableNamespaces;

    private Set<WikipediaNamespace> indexableWikipediaNamespaces;

    @PostConstruct
    public void init() {
        this.indexableWikipediaNamespaces =
            indexableNamespaces.stream().map(WikipediaNamespace::valueOf).collect(Collectors.toUnmodifiableSet());
    }

    boolean isPageIndexableByNamespace(WikipediaPage page) {
        return indexableWikipediaNamespaces.contains(page.getNamespace());
    }

    boolean isIndexableByTimestamp(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // If page modified in dump equals to the last indexing, always reindex.
        // If page modified in dump after last indexing, always reindex.
        // If page modified in dump before last indexing, do not index.
        LocalDate dbDate = Optional.ofNullable(dbPage).map(IndexablePage::getLastUpdate).orElse(null);
        if (dbDate == null) {
            return true;
        } else {
            return !page.getLastUpdate().toLocalDate().isBefore(dbDate);
        }
    }

    boolean isIndexableByPageTitle(WikipediaPage page, @Nullable IndexablePage dbPage) {
        // In case the page title has changed we force the page indexing
        String dbTitle = dbPage == null ? null : dbPage.getTitle();
        return !page.getTitle().equals(dbTitle);
    }
}
