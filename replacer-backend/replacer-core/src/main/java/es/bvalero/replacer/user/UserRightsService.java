package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.StandardType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service to check the rights of a user to use some services of the application */
@Slf4j
@Service
public class UserRightsService {

    public boolean isTypeForbidden(ReplacementType type, User user) {
        if (type.isCustomType()) {
            return false;
        }

        StandardType standardType = type.toStandardType();
        return (standardType.isForBots() && !user.isBot()) || (standardType.isForAdmin() && !user.isAdmin());
    }
}
