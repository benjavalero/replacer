package es.bvalero.replacer.user;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

/** OAuth access token to log in the application and be able to apply changes in Wikipedia */
@Value(staticConstructor = "of")
public class AccessToken {

    public static final String COOKIE_NAME = "access-token";
    private static final char SEPARATOR = '*';

    @NonNull
    String token;

    @NonNull
    String tokenSecret;

    // Simple methods to "stringify" the access token
    public String toCookieValue() {
        return token + SEPARATOR + tokenSecret;
    }

    public static AccessToken fromCookieValue(String cookieValue) {
        String[] tokens = StringUtils.split(cookieValue, SEPARATOR);
        return AccessToken.of(tokens[0], tokens[1]);
    }
}
