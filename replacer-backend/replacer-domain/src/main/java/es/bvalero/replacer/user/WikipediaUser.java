package es.bvalero.replacer.user;

import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;

/**
 * A user registered in Wikipedia, along with the roles in Wikipedia he/she belongs to.
 * Note that not all Wikipedia users are allowed to be Replacer users.
 */
@Value
@Builder
class WikipediaUser {

    @NonNull
    UserId id;

    @Builder.Default
    boolean autoConfirmed = false;

    @Builder.Default
    boolean bot = false;

    @Builder.Default
    boolean specialUser = false;
}
