package es.bvalero.replacer.authentication;

import lombok.Value;

@Value(staticConstructor = "of")
class User {
    private String name;
    private boolean admin;
}
