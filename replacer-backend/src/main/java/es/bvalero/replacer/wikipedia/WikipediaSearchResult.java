package es.bvalero.replacer.wikipedia;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;

@Value(staticConstructor = "of")
public class WikipediaSearchResult {

    @NonNull
    Integer total;

    @ToString.Exclude
    @NonNull
    List<Integer> pageIds;

    public static WikipediaSearchResult ofEmpty() {
        return WikipediaSearchResult.of(0, Collections.emptyList());
    }

    @TestOnly
    public boolean isEmpty() {
        return this.getPageIds().isEmpty();
    }
}
