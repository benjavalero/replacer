package es.bvalero.replacer.wikipedia.page;

import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
class EditToken {

    @NonNull
    String csrfToken;

    @NonNull
    WikipediaTimestamp timestamp; // Last update of the page to edit
}
