package es.bvalero.replacer.common.domain;

import java.util.Set;
import lombok.*;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.TestOnly;
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

    @Getter(onMethod_ = @TestOnly)
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
