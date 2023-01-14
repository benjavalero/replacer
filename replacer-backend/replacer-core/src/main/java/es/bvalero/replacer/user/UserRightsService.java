package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service to check the rights of a user to use some services of the application */
@Slf4j
@Service
public class UserRightsService {

    @Autowired
    private UserService userService;

    @VisibleForTesting
    public void validateAdminUser(WikipediaLanguage lang, String user) throws ForbiddenException {
        if (!isAdmin(lang, user)) {
            LOGGER.error("Unauthorized admin user: {} - {}", lang, user);
            throw new ForbiddenException();
        }
    }

    @VisibleForTesting
    boolean isAdmin(WikipediaLanguage lang, String username) {
        // Get the user just to check it actually exists
        return getUser(lang, username).map(User::isAdmin).orElse(false);
    }

    @VisibleForTesting
    public void validateBotUser(WikipediaLanguage lang, String user) throws ForbiddenException {
        if (!isBot(lang, user)) {
            LOGGER.error("Unauthorized bot user: {} - {}", lang, user);
            throw new ForbiddenException();
        }
    }

    @VisibleForTesting
    boolean isBot(WikipediaLanguage lang, String username) {
        return getUser(lang, username).map(User::isBot).orElse(false);
    }

    private Optional<User> getUser(WikipediaLanguage lang, String username) {
        return userService.findUserByName(lang, username);
    }

    public boolean isTypeForbidden(ReplacementType type, WikipediaLanguage lang, String user) {
        return (type.isForBots() && !isBot(lang, user)) || (type.isForAdmin() && !isAdmin(lang, user));
    }
}
