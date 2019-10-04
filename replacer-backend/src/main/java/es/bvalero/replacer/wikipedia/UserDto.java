package es.bvalero.replacer.wikipedia;

import lombok.Value;

@Value(staticConstructor = "of")
class UserDto {
    private String name;
    private boolean admin;
}
