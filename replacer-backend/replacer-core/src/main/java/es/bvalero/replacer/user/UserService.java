package es.bvalero.replacer.user;

import es.bvalero.replacer.auth.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Objects;
import java.util.Optional;
import lombok.Setter;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class UserService implements UserApi {

    // Dependency injection
    private final WikipediaUserRepository wikipediaUserRepository;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.admin.user}")
    private String adminUser;

    UserService(WikipediaUserRepository wikipediaUserRepository) {
        this.wikipediaUserRepository = wikipediaUserRepository;
    }

    @Override
    public Optional<User> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        return wikipediaUserRepository
            .findAuthenticatedUser(lang, accessToken)
            .map(wu -> convertWikipediaUser(wu, accessToken));
    }

    private User convertWikipediaUser(WikipediaUser wikipediaUser, AccessToken accessToken) {
        return User.builder()
            .id(wikipediaUser.getId())
            .accessToken(accessToken)
            .hasRights(wikipediaUser.isAutoConfirmed())
            .bot(wikipediaUser.isBot())
            .specialUser(wikipediaUser.isSpecialUser())
            .admin(isAdmin(wikipediaUser))
            .build();
    }

    private boolean isAdmin(WikipediaUser user) {
        return Objects.equals(this.adminUser, user.getId().getUsername());
    }
}
