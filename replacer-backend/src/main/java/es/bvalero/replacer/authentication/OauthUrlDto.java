package es.bvalero.replacer.authentication;

import lombok.Value;

@Value(staticConstructor = "of")
class OauthUrlDto {
    private String url;
}
