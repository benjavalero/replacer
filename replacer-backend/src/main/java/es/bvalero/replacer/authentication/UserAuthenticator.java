package es.bvalero.replacer.authentication;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserGroup;
import lombok.Setter;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserAuthenticator {

    @Autowired
    private WikipediaService wikipediaService;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.admin.user}")
    private String adminUser;

    AuthenticatedUser getAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) throws ReplacerException {
        WikipediaUser wikipediaUser = wikipediaService.getAuthenticatedUser(lang, accessToken);
        return AuthenticatedUser
            .builder()
            .name(wikipediaUser.getName())
            .hasRights(hasRights(wikipediaUser))
            .bot(isBot(wikipediaUser))
            .admin(isAdminUser(wikipediaUser.getName()))
            .token(accessToken.getToken())
            .tokenSecret(accessToken.getTokenSecret())
            .build();
    }

    private boolean hasRights(WikipediaUser wikipediaUser) {
        return wikipediaUser.getGroups().contains(WikipediaUserGroup.AUTOCONFIRMED);
    }

    private boolean isBot(WikipediaUser wikipediaUser) {
        return wikipediaUser.getGroups().contains(WikipediaUserGroup.BOT);
    }

    public boolean isAdminUser(String username) {
        return this.adminUser.equals(username);
    }
}
