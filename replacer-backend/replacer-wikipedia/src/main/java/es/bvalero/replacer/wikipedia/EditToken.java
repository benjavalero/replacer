package es.bvalero.replacer.wikipedia;

import java.time.LocalDateTime;
import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
class EditToken {

    @NonNull
    String csrfToken;

    @NonNull
    LocalDateTime timestamp; // Last update of the page to edit
}
