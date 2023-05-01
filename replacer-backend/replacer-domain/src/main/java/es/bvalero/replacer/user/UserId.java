package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import lombok.Value;
import org.springframework.lang.NonNull;

/** A key to identify a user in a specific Wikipedia */
@Value(staticConstructor = "of")
public class UserId {

    // https://es.wikipedia.org/wiki/Wikipedia:Restricciones_t%C3%A9cnicas_en_t%C3%ADtulos#Restricciones_en_los_nombres_de_usuario
    private static final int MAX_USERNAME_LENGTH = 40;

    @NonNull
    WikipediaLanguage lang;

    @NonNull
    String username;

    private UserId(WikipediaLanguage lang, String username) {
        if (username.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException("Too long username: " + username);
        }
        this.lang = lang;
        this.username = username;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", this.lang, this.username);
    }
}
