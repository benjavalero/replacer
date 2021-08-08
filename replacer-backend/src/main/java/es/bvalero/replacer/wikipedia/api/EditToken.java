package es.bvalero.replacer.wikipedia.api;

import java.time.LocalDateTime;
import lombok.Value;

@Value(staticConstructor = "of")
class EditToken {

    String csrfToken;
    LocalDateTime timestamp; // Last update of the page to edit
}
