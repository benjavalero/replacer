package es.bvalero.replacer.wikipedia;

import lombok.Value;

@Value
class EditToken {
    private String csrftoken;
    private String timestamp;
}
