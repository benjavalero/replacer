package es.bvalero.replacer.wikipedia;

import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

@Value
@Builder
public class WikipediaPageSaveResult {

    @NonNull
    int oldRevisionId;

    @NonNull
    int newRevisionId;

    @NonNull
    WikipediaTimestamp newTimestamp;

    @TestOnly
    public static WikipediaPageSaveResult ofDummy() {
        return WikipediaPageSaveResult.builder().newTimestamp(WikipediaTimestamp.now()).build();
    }
}
