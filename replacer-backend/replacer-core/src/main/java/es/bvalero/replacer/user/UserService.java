package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserGroup;
import es.bvalero.replacer.wikipedia.WikipediaUserRepository;
import java.util.Objects;
import java.util.Optional;
import lombok.Setter;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service to retrieve details about the users of the application */
@Service
public class UserService {

    // Dependency injection
    private final WikipediaUserRepository wikipediaUserRepository;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.admin.user}")
    private String adminUser;

    public UserService(WikipediaUserRepository wikipediaUserRepository) {
        this.wikipediaUserRepository = wikipediaUserRepository;
    }

    public Optional<User> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        return wikipediaUserRepository.findAuthenticatedUser(lang, accessToken).map(u -> convert(u, accessToken));
    }

    private User convert(WikipediaUser wikipediaUser, AccessToken accessToken) {
        return User
            .builder()
            .id(wikipediaUser.getId())
            .accessToken(accessToken)
            .hasRights(hasRights(wikipediaUser))
            .bot(isBot(wikipediaUser))
            .admin(isAdmin(wikipediaUser))
            .build();
    }

    private boolean hasRights(WikipediaUser user) {
        return user.getGroups().contains(WikipediaUserGroup.AUTO_CONFIRMED);
    }

    private boolean isBot(WikipediaUser user) {
        return user.getGroups().contains(WikipediaUserGroup.BOT);
    }

    private boolean isAdmin(WikipediaUser user) {
        return Objects.equals(this.adminUser, user.getId().getUsername());
    }
}
