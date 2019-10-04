package es.bvalero.replacer.wikipedia;

import lombok.Value;

@Value(staticConstructor = "of")
class WikipediaUser {
    private String name;
    private boolean admin;
}
