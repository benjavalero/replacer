package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
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

    UserService(WikipediaUserRepository wikipediaUserRepository) {
        this.wikipediaUserRepository = wikipediaUserRepository;
    }

    public Optional<User> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        return wikipediaUserRepository.findAuthenticatedUser(lang, accessToken).map(this::decorateAdminRole);
    }

    private User decorateAdminRole(User wikipediaUser) {
        return User.ofAdmin(wikipediaUser, isAdmin(wikipediaUser));
    }

    private boolean isAdmin(User user) {
        return Objects.equals(this.adminUser, user.getId().getUsername());
    }
}
