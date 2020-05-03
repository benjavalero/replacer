package es.bvalero.replacer.wikipedia;

import lombok.Value;

@Value(staticConstructor = "of")
class EditToken {
    String csrfToken;
    String timestamp;
}
