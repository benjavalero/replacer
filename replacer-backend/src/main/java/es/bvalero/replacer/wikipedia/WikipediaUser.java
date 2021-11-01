package es.bvalero.replacer.wikipedia;

import java.util.List;
import lombok.Value;

/** Domain object representing a user account in Wikipedia */
@Value(staticConstructor = "of")
public class WikipediaUser {

    String name;
    List<WikipediaUserGroup> groups;
    boolean admin;

    public boolean isBot() {
        return this.groups.contains(WikipediaUserGroup.BOT);
    }

    public boolean hasRights() {
        return this.groups.contains(WikipediaUserGroup.AUTOCONFIRMED);
    }
}
