package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserGroup;
import es.bvalero.replacer.wikipedia.WikipediaUserRepository;
import java.util.Objects;
import java.util.Optional;
import lombok.Setter;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service to retrieve details about the users of the application */
@Service
class UserService {

    @Autowired
    private WikipediaUserRepository wikipediaUserRepository;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.admin.user}")
    private String adminUser;

    public Optional<User> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        return wikipediaUserRepository.findAuthenticatedUser(lang, accessToken).map(this::convert);
    }

    Optional<User> findUserById(UserId userId) {
        return wikipediaUserRepository.findById(userId).map(this::convert);
    }

    private User convert(WikipediaUser wikipediaUser) {
        return User
            .builder()
            .id(wikipediaUser.getId())
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
