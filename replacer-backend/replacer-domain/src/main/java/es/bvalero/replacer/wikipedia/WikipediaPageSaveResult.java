package es.bvalero.replacer.wikipedia;

import lombok.Builder;
import lombok.Value;
import org.jspecify.annotations.NonNull;

@Value
@Builder
public class WikipediaPageSaveResult {

    int oldRevisionId;

    int newRevisionId;

    @NonNull
    WikipediaTimestamp newTimestamp;
}
