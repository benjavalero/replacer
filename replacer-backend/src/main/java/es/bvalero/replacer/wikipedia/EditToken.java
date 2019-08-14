package es.bvalero.replacer.wikipedia;

import lombok.Value;

@Value(staticConstructor = "of")
class EditToken {
    private String csrftoken;
    private String timestamp;
}
