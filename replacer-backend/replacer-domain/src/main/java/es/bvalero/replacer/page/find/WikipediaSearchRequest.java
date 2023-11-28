package es.bvalero.replacer.page.find;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import java.util.Collection;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;

/**
 * A group of options to search the pages in Wikipedia containing a given text.
 * The search will be case-insensitive if not specified otherwise.
 */
@Value
@Builder
class WikipediaSearchRequest {

    @NonNull
    WikipediaLanguage lang;

    @NonNull
    Collection<WikipediaNamespace> namespaces;

    @NonNull
    String text;

    @Builder.Default
    boolean caseSensitive = false;

    // Pagination
    int offset;
    int limit;

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
