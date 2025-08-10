package es.bvalero.replacer.wikipedia.user;

import es.bvalero.replacer.auth.AccessToken;
import es.bvalero.replacer.common.domain.UserId;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserRepository;
import es.bvalero.replacer.wikipedia.api.WikipediaApiHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import es.bvalero.replacer.wikipedia.api.WikipediaApiVerb;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Qualifier("wikipediaUserApiRepository")
@Service
@Profile("!offline")
class WikipediaUserApiRepository implements WikipediaUserRepository {

    private static final String GROUP_AUTO_CONFIRMED = "autoconfirmed";
    private static final String GROUP_BOT = "bot";

    // Special users
    private static final String ROLL_BACKER = "rollbacker";
    private static final String AUTO_VERIFIED = "autopatrolled";
    private static final String VERIFIER = "patroller";
    private static final String BUREAUCRAT = "bureaucrat";
    private static final String SYSOP = "sysop";
    private static final Set<String> SPECIAL_GROUPS = Set.of(
        ROLL_BACKER,
        AUTO_VERIFIED,
        VERIFIER,
        BUREAUCRAT,
        SYSOP,
        GROUP_BOT
    );

    // Dependency injection
    private final WikipediaApiHelper wikipediaApiHelper;

    WikipediaUserApiRepository(WikipediaApiHelper wikipediaApiHelper) {
        this.wikipediaApiHelper = wikipediaApiHelper;
    }

    @Override
    public Optional<WikipediaUser> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        WikipediaApiRequest apiRequest = WikipediaApiRequest.builder()
            .verb(WikipediaApiVerb.GET)
            .lang(lang)
            .params(buildUserInfoRequestParams())
            .accessToken(accessToken)
            .build();
        try {
            WikipediaApiResponse apiResponse = wikipediaApiHelper.executeApiRequest(apiRequest);
            return Optional.of(extractUserInfoFromJson(apiResponse, lang));
        } catch (WikipediaException e) {
            LOGGER.error("Error finding authenticated user", e);
        }
        return Optional.empty();
    }

    private Map<String, String> buildUserInfoRequestParams() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "query");
        params.put("meta", "userinfo");
        params.put("uiprop", "groups");
        return params;
    }

    private WikipediaUser extractUserInfoFromJson(WikipediaApiResponse response, WikipediaLanguage lang) {
        return convertUserInfo(response.getQuery().getUserinfo(), lang);
    }

    private WikipediaUser convertUserInfo(WikipediaApiResponse.UserInfo userInfo, WikipediaLanguage lang) {
        UserId userId = UserId.of(lang, userInfo.getName());
        boolean isAutoConfirmed = userInfo.getGroups().contains(GROUP_AUTO_CONFIRMED);
        boolean isBot = userInfo.getGroups().contains(GROUP_BOT);
        boolean isSpecialUser = userInfo.getGroups().stream().anyMatch(SPECIAL_GROUPS::contains);
        return WikipediaUser.builder()
            .id(userId)
            .autoConfirmed(isAutoConfirmed)
            .bot(isBot)
            .specialUser(isSpecialUser)
            .build();
    }
}
