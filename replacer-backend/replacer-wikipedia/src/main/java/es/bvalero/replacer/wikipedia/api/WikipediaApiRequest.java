package es.bvalero.replacer.wikipedia.api;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Map;
import lombok.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * A group of common arguments when performing a request to Wikipedia API.
 * The language is important because it defines which Wikipedia will be called.
 * The access token is optional, as most requests don't need to be signed.
 */
@Value
@Builder(toBuilder = true)
public class WikipediaApiRequest {

    @NonNull
    WikipediaApiVerb verb;

    @Getter(AccessLevel.NONE)
    @NonNull
    WikipediaLanguage lang;

    @Singular
    Map<String, String> params;

    @Nullable
    AccessToken accessToken;

    String getUrl() {
        return String.format("https://%s.wikipedia.org/w/api.php", this.lang.getCode());
    }

    boolean isSigned() {
        // Access token can be empty in tests
        return this.accessToken != null;
    }
}
