package es.bvalero.replacer.wikipedia.api;

import lombok.Value;

@Value(staticConstructor = "of")
class EditToken {

    String csrfToken;
    String timestamp;
}
