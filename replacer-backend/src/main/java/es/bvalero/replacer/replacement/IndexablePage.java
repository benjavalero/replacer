package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.VisibleForTesting;

@Value
@Builder
public class IndexablePage {

    int id;
    WikipediaLanguage lang;
    String title;
    WikipediaNamespace namespace;
    LocalDate lastUpdate;
    String content;

    // Throw an exception instead of returning a boolean to capture the cause
    public void validateProcessable() throws ReplacerException {
        validateProcessableByNamespace();
    }

    @VisibleForTesting
    void validateProcessableByNamespace() throws ReplacerException {
        if (!WikipediaNamespace.getProcessableNamespaces().contains(getNamespace())) {
            throw new ReplacerException("Page not processable by namespace: " + getNamespace());
        }
    }

    @Override
    public String toString() {
        return (
            "IndexablePage(id=" +
            this.getId() +
            ", lang=" +
            this.getLang().getCode() +
            ", title=" +
            this.getTitle() +
            ")"
        );
    }
}
