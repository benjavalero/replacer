package es.bvalero.replacer.page.validate;

import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PageValidator {

    @Value("${replacer.processable.namespaces}")
    private Set<Integer> processableNamespaces;

    private Set<WikipediaNamespace> processableWikipediaNamespaces;

    @PostConstruct
    public void init() {
        this.processableWikipediaNamespaces =
            processableNamespaces.stream().map(WikipediaNamespace::valueOf).collect(Collectors.toSet());
    }

    // Throw an exception instead of returning a boolean to capture the cause
    public void validateProcessable(ValidatePage page) throws ReplacerException {
        validateProcessableByNamespace(page);
        // Validation by content to find redirections is not done here anymore
        // but as an immutable covering the whole content
    }

    private void validateProcessableByNamespace(ValidatePage page) throws ReplacerException {
        if (!processableWikipediaNamespaces.contains(page.getNamespace())) {
            throw new ReplacerException("Page not processable by namespace: " + page.getNamespace());
        }
    }
}
