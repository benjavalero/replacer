package es.bvalero.replacer.user;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;

/**
 * A user of the application, which has also to be a Wikipedia user.
 * Note that a Wikipedia user belongs to several groups allowing to perform different tasks.
 * A Replacer user also has rights to perform different tasks, being these rights inferred
 * from the Wikipedia groups but also by configuration.
 * Therefore, it is better the keep two different classes for Wikipedia and application users,
 * to separate clearly the Wikipedia rights logic from the on in Replacer rights.
 * Precisely because of this access to the configuration, the logic is not implemented
 * in the domain object itself, but in a related but separated service.
 */
@Value
@Builder
class User {

    @NonNull
    UserId id;

    /** If the user is allowed to use the application as a standard user */
    @Builder.Default
    @Accessors(fluent = true)
    boolean hasRights = false;

    /** If the user is allowed to perform tasks in the application restricted to bots */
    @Builder.Default
    boolean bot = false;

    /** It the user is allowed to perform tasks in the application restricted to administrators */
    @Builder.Default
    boolean admin = false;
}
