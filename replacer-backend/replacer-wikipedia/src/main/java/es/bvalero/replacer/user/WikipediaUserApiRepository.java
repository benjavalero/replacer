package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.api.WikipediaApiHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import es.bvalero.replacer.wikipedia.api.WikipediaApiVerb;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Qualifier("wikipediaUserApiRepository")
@Service
@Profile("!offline")
class WikipediaUserApiRepository implements WikipediaUserRepository {

    // Dependency injection
    private final WikipediaApiHelper wikipediaApiHelper;

    WikipediaUserApiRepository(WikipediaApiHelper wikipediaApiHelper) {
        this.wikipediaApiHelper = wikipediaApiHelper;
    }

    @Override
    public Optional<User> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiVerb.GET)
            .lang(lang)
            .params(buildUserInfoRequestParams())
            .accessToken(accessToken)
            .build();
        try {
            WikipediaApiResponse apiResponse = wikipediaApiHelper.executeApiRequest(apiRequest);
            return Optional
                .of(extractUserInfoFromJson(apiResponse, lang))
                .map(wu -> convertWikipediaUser(wu, accessToken));
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
        return WikipediaUser.of(UserId.of(lang, userInfo.getName()), convertGroups(userInfo.getGroups()));
    }

    private Collection<WikipediaUserGroup> convertGroups(Collection<String> userGroups) {
        return userGroups
            .stream()
            .map(WikipediaUserGroup::valueOfLabel)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());
    }

    private User convertWikipediaUser(WikipediaUser wikipediaUser, AccessToken accessToken) {
        return User
            .builder()
            .id(wikipediaUser.getId())
            .accessToken(accessToken)
            .hasRights(hasRights(wikipediaUser))
            .bot(isBot(wikipediaUser))
            .build();
    }

    private boolean hasRights(WikipediaUser user) {
        return user.getGroups().contains(WikipediaUserGroup.AUTO_CONFIRMED);
    }

    private boolean isBot(WikipediaUser user) {
        return user.getGroups().contains(WikipediaUserGroup.BOT);
    }
}
