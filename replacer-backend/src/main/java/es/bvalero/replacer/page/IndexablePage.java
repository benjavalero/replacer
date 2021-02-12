package es.bvalero.replacer.page;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.time.LocalDate;
import java.util.List;
import org.jetbrains.annotations.VisibleForTesting;

public interface IndexablePage {
    int getId();

    WikipediaLanguage getLang();

    String getTitle();

    WikipediaNamespace getNamespace();

    String getContent();

    LocalDate getLastUpdate();

    // Throw an exception instead of returning a boolean to capture the cause
    default void validateProcessable(List<String> ignorableTemplates) throws ReplacerException {
        validateProcessableByNamespace();
        validateProcessableByContent(ignorableTemplates);
    }

    @VisibleForTesting
    default void validateProcessableByNamespace() throws ReplacerException {
        if (!WikipediaNamespace.getProcessableNamespaces().contains(getNamespace())) {
            throw new ReplacerException("Page not processable by namespace: " + getNamespace());
        }
    }

    @VisibleForTesting
    default void validateProcessableByContent(List<String> ignorableTemplates) throws ReplacerException {
        String lowerContent = getContent().toLowerCase();
        for (String template : ignorableTemplates) {
            int start = lowerContent.indexOf(template);
            if (start >= 0 && FinderUtils.isWordCompleteInText(start, template, lowerContent)) {
                throw new ReplacerException("Page not processable by content: " + template);
            }
        }
    }

    default String getContext(Replacement replacement) {
        return FinderUtils.getContextAroundWord(this.getContent(), replacement.getStart(), replacement.getEnd(), 20);
    }
}
