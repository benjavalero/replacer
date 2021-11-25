package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Validates if a Wikipedia page is processable, in particular by namespace.
 *
 * It is a Component because the processable namespaces are configurable,
 * and thus they are not known until the application is running.
 */
@Component
class PageIndexValidator {

    @Value("${replacer.processable.namespaces}")
    private Set<Integer> processableNamespaces;

    private Set<WikipediaNamespace> processableWikipediaNamespaces;

    @PostConstruct
    public void init() {
        this.processableWikipediaNamespaces =
            processableNamespaces.stream().map(WikipediaNamespace::valueOf).collect(Collectors.toUnmodifiableSet());
    }

    // Throw an exception instead of returning a boolean to capture the cause
    void validateProcessable(WikipediaPage page) throws PageNotProcessableException {
        validateProcessableByNamespace(page);
        // Validation by content to find redirections is not done here anymore
        // but as an immutable covering the whole content
    }

    private void validateProcessableByNamespace(WikipediaPage page) throws PageNotProcessableException {
        if (!processableWikipediaNamespaces.contains(page.getNamespace())) {
            throw new PageNotProcessableException("Page not processable by namespace: " + page.getNamespace());
        }
    }
}
