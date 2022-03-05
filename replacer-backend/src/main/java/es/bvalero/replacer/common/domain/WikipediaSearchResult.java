package es.bvalero.replacer.common.domain;

import java.util.List;
import lombok.*;
import org.jetbrains.annotations.TestOnly;

@Value
@Builder
public class WikipediaSearchResult {

    int total;

    @ToString.Exclude
    @Singular
    List<Integer> pageIds;

    public static WikipediaSearchResult ofEmpty() {
        return WikipediaSearchResult.builder().total(0).build();
    }

    @TestOnly
    public boolean isEmpty() {
        return pageIds.isEmpty();
    }
}
