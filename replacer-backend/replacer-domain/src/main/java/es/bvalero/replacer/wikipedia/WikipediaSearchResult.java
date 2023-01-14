package es.bvalero.replacer.wikipedia;

import java.util.Collection;
import lombok.Builder;
import lombok.Singular;
import lombok.ToString;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;

@Value
@Builder
public class WikipediaSearchResult {

    int total;

    @ToString.Exclude
    @Singular
    Collection<Integer> pageIds;

    public static WikipediaSearchResult ofEmpty() {
        return WikipediaSearchResult.builder().total(0).build();
    }

    @TestOnly
    public boolean isEmpty() {
        return this.pageIds.isEmpty();
    }
}
