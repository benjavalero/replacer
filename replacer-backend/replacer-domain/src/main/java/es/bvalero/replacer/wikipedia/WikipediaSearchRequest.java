package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class WikipediaSearchRequest {

    @NonNull
    WikipediaLanguage lang;

    @NonNull
    Collection<WikipediaNamespace> namespaces;

    @NonNull
    String text;

    @Builder.Default
    boolean caseSensitive = false;

    int offset;

    int limit;
}
