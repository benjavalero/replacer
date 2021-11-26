package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
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

    // Throw an exception instead of returning a boolean to capture the cause
    void validateIndexable(WikipediaPage page) throws NonIndexablePageException {
        validateIndexableByNamespace(page);
        // Validation by content to find redirections is not done here anymore
        // but as an immutable covering the whole content
    }

    private void validateIndexableByNamespace(WikipediaPage page) throws NonIndexablePageException {
        if (!indexableWikipediaNamespaces.contains(page.getNamespace())) {
            throw new NonIndexablePageException("Page not indexable by namespace: " + page.getNamespace());
        }
    }
}
