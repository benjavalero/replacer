package es.bvalero.replacer.wikipedia;

import java.util.List;
import lombok.*;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

@Value
@Builder
public class WikipediaSearchResult {

    @NonNull
    Integer total;

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
