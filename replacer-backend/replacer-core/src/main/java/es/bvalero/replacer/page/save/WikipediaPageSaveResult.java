package es.bvalero.replacer.page.save;

import es.bvalero.replacer.page.find.WikipediaTimestamp;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

@Value
@Builder
class WikipediaPageSaveResult {

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
