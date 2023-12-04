package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.api.WikipediaApiHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import es.bvalero.replacer.wikipedia.api.WikipediaApiVerb;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

    // Dependency injection
    private final WikipediaApiHelper wikipediaApiHelper;

    WikipediaUserApiRepository(WikipediaApiHelper wikipediaApiHelper) {
        this.wikipediaApiHelper = wikipediaApiHelper;
    }

    @Override
    public Optional<WikipediaUser> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
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
        return WikipediaUser.of(userId, isAutoConfirmed, isBot);
    }
}
