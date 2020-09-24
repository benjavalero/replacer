package es.bvalero.replacer.page;

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

    default boolean isProcessable(List<String> ignorableTemplates) {
        return isProcessableByNamespace() && isProcessableByContent(ignorableTemplates);
    }

    default boolean isProcessableByNamespace() {
        return WikipediaNamespace.getProcessableNamespaces().contains(getNamespace());
    }

    default boolean isProcessableByContent(List<String> ignorableTemplates) {
        String lowerContent = getContent().toLowerCase();
        return ignorableTemplates.stream().noneMatch(lowerContent::contains);
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
