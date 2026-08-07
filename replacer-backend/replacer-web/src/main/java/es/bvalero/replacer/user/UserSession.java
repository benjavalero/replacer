package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

/** Aggregates the language and access token that identify a user session, as stored in the session cookie */
@Value(staticConstructor = "of")
class UserSession {

    private static final String SEPARATOR = "|";

    @NonNull
    WikipediaLanguage lang;

    @NonNull
    AccessToken accessToken;

    String toCookieValue() {
        return lang.getCode() + SEPARATOR + accessToken.toCookieValue();
    }

    static UserSession fromCookieValue(String cookieValue) {
        String[] parts = StringUtils.split(cookieValue, SEPARATOR, 2);
        return UserSession.of(WikipediaLanguage.valueOfCode(parts[0]), AccessToken.fromCookieValue(parts[1]));
    }
}
