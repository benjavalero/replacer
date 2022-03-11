package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.wikipedia.WikipediaUserRepository;
import lombok.Setter;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/** Service to retrieve details about the users of the application */
@Service
public class UserService {

    @Autowired
    private WikipediaUserRepository wikipediaUserRepository;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.admin.user}")
    private String adminUser;

    public Optional<ReplacerUser> findUser(WikipediaLanguage lang, AccessToken accessToken) {
        return wikipediaUserRepository.findAuthenticatedUser(lang, accessToken).map(this::convert);
    }

    Optional<ReplacerUser> findUser(WikipediaLanguage lang, String username) {
        return wikipediaUserRepository.findByUsername(lang, username).map(this::convert);
    }

    private ReplacerUser convert(WikipediaUser wikipediaUser) {
        return ReplacerUser.builder()
            .lang(wikipediaUser.getLang())
            .name(wikipediaUser.getName())
            .hasRights(wikipediaUser.getGroups().contains(WikipediaUserGroup.AUTO_CONFIRMED))
            .bot(wikipediaUser.getGroups().contains(WikipediaUserGroup.BOT))
            .admin(Objects.equals(this.adminUser, wikipediaUser.getName()))
            .build();
    }
}
