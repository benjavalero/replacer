package es.bvalero.replacer.page.save;

import es.bvalero.replacer.page.find.WikipediaTimestamp;
import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
class EditToken {

    @NonNull
    String csrfToken;

    @NonNull
    WikipediaTimestamp timestamp; // Last update of the page to edit
}
