package es.bvalero.replacer.user;

import lombok.Value;
import org.springframework.lang.NonNull;

/**
 * A user registered in Wikipedia, along with the roles in Wikipedia he/she belongs to.
 * Note that not all Wikipedia users are allowed to be Replacer users.
 */
@Value(staticConstructor = "of")
class WikipediaUser {

    @NonNull
    UserId id;

    boolean autoConfirmed;
    boolean bot;
}
