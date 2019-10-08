package es.bvalero.replacer.article;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public interface IndexableArticle {
    String PREFIX_REDIRECT = "#redirec";
    String TEMPLATE_DESTROY = "{{destruir";
    String TEMPLATE_COPY_EDIT = "{{copyedit";

    int getId();

    String getTitle();

    WikipediaNamespace getNamespace();

    String getContent();

    LocalDateTime getLastUpdate();

    default boolean isProcessable() {
        if (!isProcessableByNamespace()) {
            return false;
        }
        return isProcessableByContent();
    }

    default boolean isProcessableByNamespace() {
        return WikipediaNamespace.getProcessableNamespaces().contains(getNamespace());
    }

    default boolean isProcessableByContent() {
        String lowerContent = getContent().toLowerCase();
        List<String> templatesNotProcessable =
                Arrays.asList(PREFIX_REDIRECT, TEMPLATE_DESTROY, TEMPLATE_COPY_EDIT);
        return templatesNotProcessable.stream().noneMatch(lowerContent::contains);
    }

}
