package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.user.UserId;
import java.util.Collection;
import lombok.Value;
import org.springframework.lang.NonNull;

/**
 * A user registered in Wikipedia, along with the roles in Wikipedia he/she belongs to.
 * Note that not all Wikipedia users are allowed to be Replacer users.
 */
@Value(staticConstructor = "of")
public class WikipediaUser {

    @NonNull
    UserId id;

    @NonNull
    Collection<WikipediaUserGroup> groups;
}
