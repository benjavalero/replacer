package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Set;
import lombok.*;
import lombok.experimental.NonFinal;
import org.springframework.lang.NonNull;

/** User in Wikipedia */
@NonFinal
@Value
@Builder
public class WikipediaUser {

    @NonNull
    WikipediaLanguage lang;

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
