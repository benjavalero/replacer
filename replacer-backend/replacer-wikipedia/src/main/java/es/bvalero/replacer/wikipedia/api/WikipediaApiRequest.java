package es.bvalero.replacer.wikipedia.api;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.AccessToken;
import java.util.Map;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Value
@Builder(toBuilder = true)
public class WikipediaApiRequest {

    @NonNull
    WikipediaApiRequestVerb verb;

    @NonNull
    WikipediaLanguage lang;

    @Singular
    Map<String, String> params;

    @Nullable
    AccessToken accessToken;
}
