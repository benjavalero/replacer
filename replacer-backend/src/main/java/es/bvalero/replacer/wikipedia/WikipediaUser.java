package es.bvalero.replacer.wikipedia;

import java.util.List;
import lombok.Value;
import org.springframework.lang.NonNull;

/** User in Wikipedia */
@Value(staticConstructor = "of")
public class WikipediaUser {

    @NonNull
    String name;

    @NonNull
    List<WikipediaUserGroup> groups;
}
