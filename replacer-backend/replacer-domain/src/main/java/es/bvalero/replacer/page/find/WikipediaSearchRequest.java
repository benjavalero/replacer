package es.bvalero.replacer.page.find;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.util.Collection;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;

/**
 * A group of options to search the pages in Wikipedia containing a given text.
 * The search will be case-insensitive if not specified otherwise.
 * Also by default it will only search in the article namespace.
 */
@Value
@Builder
public class WikipediaSearchRequest {

    @NonNull
    WikipediaLanguage lang;

    @Builder.Default
    Collection<WikipediaNamespace> namespaces = List.of(WikipediaNamespace.getDefault());

    @NonNull
    String text;

    @Builder.Default
    boolean caseSensitive = false;

    // Pagination
    @Builder.Default
    int offset = 0;

    @Builder.Default
    int limit = WikipediaPageRepository.MAX_SEARCH_RESULTS;

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
