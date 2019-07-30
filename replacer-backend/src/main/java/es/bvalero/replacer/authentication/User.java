package es.bvalero.replacer.authentication;

import lombok.Value;

@Value
class User {
    private String name;
    private boolean admin;
}
