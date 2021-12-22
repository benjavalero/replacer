package es.bvalero.replacer.wikipedia;

import java.util.Set;
import lombok.*;
import org.springframework.lang.NonNull;

/** User in Wikipedia */
@Value
@Builder
public class WikipediaUser {

    @NonNull
    String name;

    @Getter(AccessLevel.NONE)
    @Singular
    Set<WikipediaUserGroup> groups;

    public boolean hasRights() {
        return this.groups.contains(WikipediaUserGroup.AUTO_CONFIRMED);
    }

    public boolean isBot() {
        return this.groups.contains(WikipediaUserGroup.BOT);
    }
    // We cannot add here the logic to check if the user is administrator of the tool
    // because this is something configurable
}
