package es.bvalero.replacer.page;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.time.LocalDate;
import org.jetbrains.annotations.VisibleForTesting;

public interface IndexablePage {
    int getId();

    WikipediaLanguage getLang();

    String getTitle();

    WikipediaNamespace getNamespace();

    String getContent();

    LocalDate getLastUpdate();

    // Throw an exception instead of returning a boolean to capture the cause
    default void validateProcessable() throws ReplacerException {
        validateProcessableByNamespace();
    }

    @VisibleForTesting
    default void validateProcessableByNamespace() throws ReplacerException {
        if (!WikipediaNamespace.getProcessableNamespaces().contains(getNamespace())) {
            throw new ReplacerException("Page not processable by namespace: " + getNamespace());
        }
    }

    default String getContext(Replacement replacement) {
        return FinderUtils.getContextAroundWord(this.getContent(), replacement.getStart(), replacement.getEnd(), 20);
    }
}
