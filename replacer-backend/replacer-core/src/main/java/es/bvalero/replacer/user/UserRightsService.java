package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.StandardType;
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

    public void validateAdminUser(UserId userId) throws ForbiddenException {
        if (!isAdmin(userId)) {
            LOGGER.error("Unauthorized admin user: {}", userId);
            throw new ForbiddenException();
        }
    }

    @VisibleForTesting
    boolean isAdmin(UserId userId) {
        // Get the user just to check it actually exists
        return getUser(userId).map(User::isAdmin).orElse(false);
    }

    public void validateBotUser(UserId userId) throws ForbiddenException {
        if (!isBot(userId)) {
            LOGGER.error("Unauthorized bot user: {}", userId);
            throw new ForbiddenException();
        }
    }

    @VisibleForTesting
    boolean isBot(UserId userId) {
        return getUser(userId).map(User::isBot).orElse(false);
    }

    private Optional<User> getUser(UserId userId) {
        return userService.findUserById(userId);
    }

    public boolean isTypeForbidden(ReplacementType type, UserId userId) {
        if (type.isCustomType()) {
            return false;
        }

        StandardType standardType = type.toStandardType();
        return (standardType.isForBots() && !isBot(userId)) || (standardType.isForAdmin() && !isAdmin(userId));
    }
}
