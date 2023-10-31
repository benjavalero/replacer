package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.user.UserId;
import es.bvalero.replacer.wikipedia.api.WikipediaApiHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import es.bvalero.replacer.wikipedia.api.WikipediaApiVerb;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
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

    @Override
    public Optional<WikipediaUser> findById(UserId userId) {
        WikipediaLanguage lang = userId.getLang();
        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiVerb.GET)
            .lang(lang)
            .params(buildUserRequestParams(userId.getUsername()))
            .build();
        try {
            WikipediaApiResponse apiResponse = wikipediaApiHelper.executeApiRequest(apiRequest);
            return extractUserFromJson(apiResponse, lang);
        } catch (WikipediaException e) {
            LOGGER.error("Error finding user by ID: {}", userId, e);
        }
        return Optional.empty();
    }

    @Nullable
    private WikipediaUser convertUser(WikipediaApiResponse.User user, WikipediaLanguage lang) {
        if (user.isMissing()) {
            LOGGER.warn("Missing user: {}", user.getName());
            return null;
        }

        return WikipediaUser.of(UserId.of(lang, user.getName()), convertGroups(user.getGroups()));
    }

    private Map<String, String> buildUserRequestParams(String username) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "query");
        params.put("list", "users");
        params.put("ususers", username);
        params.put("usprop", "groups");
        return params;
    }

    private Optional<WikipediaUser> extractUserFromJson(WikipediaApiResponse response, WikipediaLanguage lang) {
        return response.getQuery().getUsers().stream().findFirst().map(u -> this.convertUser(u, lang));
    }
}
