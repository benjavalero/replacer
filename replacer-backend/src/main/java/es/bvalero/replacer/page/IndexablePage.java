package es.bvalero.replacer.page;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.replacement.IndexableReplacement;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.time.LocalDate;
import java.util.List;

public interface IndexablePage {
    int getId();

    WikipediaLanguage getLang();

    String getTitle();

    WikipediaNamespace getNamespace();

    String getContent();

    LocalDate getLastUpdate();

    default void validateProcessable(List<String> ignorableTemplates) throws ReplacerException {
        validateProcessableByNamespace();
        validateProcessableByContent(ignorableTemplates);
    }

    default void validateProcessableByNamespace() throws ReplacerException {
        if (!WikipediaNamespace.getProcessableNamespaces().contains(getNamespace())) {
            throw new ReplacerException("Page not processable by namespace: " + getNamespace());
        }
    }

    default void validateProcessableByContent(List<String> ignorableTemplates) throws ReplacerException {
        String lowerContent = getContent().toLowerCase();
        for (String template : ignorableTemplates) {
            int start = lowerContent.indexOf(template);
            if (start >= 0 && FinderUtils.isWordCompleteInText(start, template, lowerContent)) {
                throw new ReplacerException("Page not processable by content: " + template);
            }
        }
    }

    default IndexableReplacement convertReplacementToIndexed(Replacement replacement) {
        return IndexableReplacement.of(
            this.getId(),
            this.getLang(),
            replacement.getType(),
            replacement.getSubtype(),
            replacement.getStart(),
            replacement.getContext(),
            this.getLastUpdate(),
            this.getTitle()
        );
    }
}
