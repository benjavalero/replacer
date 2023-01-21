package es.bvalero.replacer.wikipedia;

import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
class EditToken {

    @NonNull
    String csrfToken;

    @NonNull
    WikipediaTimestamp timestamp; // Last update of the page to edit
}
