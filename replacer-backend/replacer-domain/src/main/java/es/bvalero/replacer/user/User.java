package es.bvalero.replacer.user;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;

/** User of the application (existing in Wikipedia) */
@Value
@Builder
class User {

    @NonNull
    UserId id;

    @Builder.Default
    @Accessors(fluent = true)
    boolean hasRights = false;

    @Builder.Default
    boolean bot = false;

    @Builder.Default
    boolean admin = false;
}
