package es.bvalero.replacer.wikipedia;

import java.util.Collection;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/**
 * A collection of page IDs as a result from a search request,
 * along with the total number of results without pagination.
 * As one of the mandatory search parameters is the language of the Wikipedia,
 * we can assume all the page IDs belong to the same Wikipedia,
 * so we can use directly the page IDs as numbers.
 */
@Value
@Builder
public class WikipediaSearchResult {

    int total;

    @Singular
    Collection<Integer> pageIds;

    public static WikipediaSearchResult ofEmpty() {
        return WikipediaSearchResult.builder().total(0).build();
    }

    public boolean isEmpty() {
        return this.pageIds.isEmpty();
    }

    @Override
    public String toString() {
        return Integer.toString(this.total);
    }
}
