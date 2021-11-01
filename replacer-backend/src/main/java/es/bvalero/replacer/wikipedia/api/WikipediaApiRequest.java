package es.bvalero.replacer.wikipedia.api;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.domain.AccessToken;
import java.util.Map;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.springframework.lang.Nullable;

@Value
@Builder(toBuilder = true)
class WikipediaApiRequest {

    WikipediaApiRequestVerb verb;
    WikipediaLanguage lang;

    @Singular
    Map<String, String> params;

    @Nullable
    AccessToken accessToken;
}
