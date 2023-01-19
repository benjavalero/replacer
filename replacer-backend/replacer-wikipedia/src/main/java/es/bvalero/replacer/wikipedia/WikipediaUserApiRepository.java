package es.bvalero.replacer.wikipedia;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequestHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequestVerb;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/** Wikipedia service implementation using classic Wikipedia API */
@Slf4j
@Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 10, warnUnit = TimeUnit.SECONDS)
@Qualifier("wikipediaUserApiRepository")
@Service
@Profile("!offline")
class WikipediaUserApiRepository implements WikipediaUserRepository {

    @Autowired
    private WikipediaApiRequestHelper wikipediaApiRequestHelper;

    @Override
    public Optional<WikipediaUser> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.GET)
            .lang(lang)
            .params(buildUserInfoRequestParams())
            .accessToken(accessToken)
            .build();
        try {
            WikipediaApiResponse apiResponse = wikipediaApiRequestHelper.executeApiRequest(apiRequest);
            return Optional.of(extractUserInfoFromJson(apiResponse));
        } catch (WikipediaException e) {
            LOGGER.error("Error finding authenticated user", e);
        }
        return Optional.empty();
    }

    private WikipediaUser convertUserInfo(WikipediaApiResponse.UserInfo userInfo) {
        return WikipediaUser.of(userInfo.getName(), convertGroups(userInfo.getGroups()));
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

    private WikipediaUser extractUserInfoFromJson(WikipediaApiResponse response) {
        return convertUserInfo(response.getQuery().getUserinfo());
    }

    @Override
    public Optional<WikipediaUser> findByUsername(WikipediaLanguage lang, String username) {
        WikipediaApiRequest apiRequest = WikipediaApiRequest
            .builder()
            .verb(WikipediaApiRequestVerb.GET)
            .lang(lang)
            .params(buildUserRequestParams(username))
            .build();
        try {
            WikipediaApiResponse apiResponse = wikipediaApiRequestHelper.executeApiRequest(apiRequest);
            return extractUserFromJson(apiResponse);
        } catch (WikipediaException e) {
            LOGGER.error("Error finding user by username: {}", username, e);
        }
        return Optional.empty();
    }

    @Nullable
    private WikipediaUser convertUser(WikipediaApiResponse.User user) {
        if (user.isMissing()) {
            LOGGER.warn("Missing user: {}", user.getName());
            return null;
        }

        return WikipediaUser.of(user.getName(), convertGroups(user.getGroups()));
    }

    private Map<String, String> buildUserRequestParams(String username) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "query");
        params.put("list", "users");
        params.put("ususers", username);
        params.put("usprop", "groups");
        return params;
    }

    private Optional<WikipediaUser> extractUserFromJson(WikipediaApiResponse response) {
        return response.getQuery().getUsers().stream().findFirst().map(this::convertUser);
    }
}
