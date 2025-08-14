package es.bvalero.replacer.wikipedia;

import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;

@Value
@Builder
public class WikipediaPageSaveResult {

    int oldRevisionId;

    int newRevisionId;

    @NonNull
    WikipediaTimestamp newTimestamp;
}
