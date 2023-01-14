package es.bvalero.replacer.wikipedia;

import java.util.Collection;
import lombok.Value;
import org.springframework.lang.NonNull;

/**
 * User in Wikipedia.
 * We consider the correspondence between the groups and the Replacer rights as business rules,
 * so we keep this class as a subdomain value-object.
 */
@Value(staticConstructor = "of")
public class WikipediaUser {

    @NonNull
    String name;

    @NonNull
    Collection<WikipediaUserGroup> groups;
}
