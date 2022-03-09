package es.bvalero.replacer.common.domain;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;

/** User of the application (existing in Wikipedia) */
@Value
@Builder
public class ReplacerUser {

    @NonNull
    WikipediaLanguage lang;

    @NonNull
    String name;

    @Accessors(fluent = true)
    boolean hasRights;

    boolean bot;

    boolean admin;
}
