package es.bvalero.replacer.user;

import es.bvalero.replacer.auth.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

/**
 * <p>A user of the application, which has also to be a Wikipedia user.
 * Note that a Wikipedia user belongs to several groups allowing to perform different tasks.
 * A Replacer user also has rights to perform different tasks, being these rights inferred
 * from the Wikipedia groups but also by configuration.</p>
 *
 * <p> Therefore, it is better the keep two different classes for Wikipedia and application users,
 * to separate clearly the Wikipedia rights logic from the on in Replacer rights.
 * Precisely because of this access to the configuration, the logic is not implemented
 * in the domain object itself, but in a related but separated service.</p>
 *
 * <p>A user of the application must always be authenticated, so we also store here the access token.</p>
 */
@Value
@Builder
public class User {

    @NonNull
    UserId id;

    @NonNull
    AccessToken accessToken;

    /** If the user is allowed to use the application as a standard user */
    @Builder.Default
    @Accessors(fluent = true)
    boolean hasRights = false;

    /** If the user is allowed to perform tasks in the application restricted to bots */
    @Builder.Default
    boolean bot = false;

    /** If the user is allowed to perform tasks in the application restricted to special uses, e.g. patrollers. */
    @Builder.Default
    boolean specialUser = false;

    /** It the user is allowed to perform tasks in the application restricted to administrators */
    @With(AccessLevel.PRIVATE)
    @Builder.Default
    boolean admin = false;

    @Override
    public String toString() {
        return this.id.toString();
    }

    @TestOnly
    public static User buildTestUser() {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "x");
        AccessToken accessToken = AccessToken.of("a", "b");
        return User.builder().id(userId).accessToken(accessToken).hasRights(true).build();
    }

    @TestOnly
    public static User buildTestBotUser() {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "bot");
        AccessToken accessToken = AccessToken.of("a", "b");
        return User.builder().id(userId).accessToken(accessToken).hasRights(true).bot(true).build();
    }

    @TestOnly
    public static User buildTestAdminUser() {
        UserId userId = UserId.of(WikipediaLanguage.getDefault(), "admin");
        AccessToken accessToken = AccessToken.of("a", "b");
        return User.builder().id(userId).accessToken(accessToken).hasRights(true).admin(true).build();
    }
}
