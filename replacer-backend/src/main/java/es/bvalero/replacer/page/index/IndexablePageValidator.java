package es.bvalero.replacer.page.index;

import es.bvalero.replacer.domain.ReplacerException;
import java.util.Set;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IndexablePageValidator {

    @Value("${replacer.processable.namespaces}")
    private Set<Integer> processableNamespaces;

    // Throw an exception instead of returning a boolean to capture the cause
    public void validateProcessable(IndexablePage page) throws ReplacerException {
        validateProcessableByNamespace(page);
    }

    @VisibleForTesting
    public void validateProcessableByNamespace(IndexablePage page) throws ReplacerException {
        if (!processableNamespaces.contains(page.getNamespace().getValue())) {
            throw new ReplacerException("Page not processable by namespace: " + page.getNamespace());
        }
    }
}
