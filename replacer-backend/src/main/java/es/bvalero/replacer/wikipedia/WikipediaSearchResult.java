package es.bvalero.replacer.wikipedia;

import java.util.Collections;
import java.util.List;
import lombok.Value;

@Value(staticConstructor = "of")
public class WikipediaSearchResult {

    long total;
    List<Integer> pageIds;

    public static WikipediaSearchResult ofEmpty() {
        return new WikipediaSearchResult(0, Collections.emptyList());
    }

    boolean isEmpty() {
        return this.getPageIds().isEmpty();
    }
}
