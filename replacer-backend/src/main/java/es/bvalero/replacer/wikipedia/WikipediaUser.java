package es.bvalero.replacer.wikipedia;

import lombok.Value;

@Value(staticConstructor = "of")
class WikipediaUser {

    String name;
    boolean admin;
    AccessToken accessToken;
}
