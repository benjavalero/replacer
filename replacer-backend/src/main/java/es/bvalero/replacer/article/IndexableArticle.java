package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.replacement.IndexableReplacement;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public interface IndexableArticle {
    int getId();

    WikipediaLanguage getLang();

    WikipediaNamespace getNamespace();

    String getContent();

    LocalDate getLastUpdate();

    default boolean isProcessable() {
        return isProcessableByNamespace() && isProcessableByContent();
    }

    default boolean isProcessableByNamespace() {
        return WikipediaNamespace.getProcessableNamespaces().contains(getNamespace());
    }

    default boolean isProcessableByContent() {
        String lowerContent = getContent().toLowerCase();
        List<String> templatesNotProcessable = Arrays.asList("#redirec", "{{destruir", "{{copyedit");
        return templatesNotProcessable.stream().noneMatch(lowerContent::contains);
    }

    default IndexableReplacement convertReplacementToIndexed(Replacement replacement) {
        return IndexableReplacement.of(
            this.getId(),
            this.getLang(),
            replacement.getType(),
            replacement.getSubtype(),
            replacement.getStart(),
            replacement.getContext(),
            this.getLastUpdate()
        );
    }
}
