package es.bvalero.replacer.authentication;

import lombok.Value;

@Value(staticConstructor = "of")
class WikipediaUser {

    String name;
    boolean admin;
    AccessToken accessToken;
}
