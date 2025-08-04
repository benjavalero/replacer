package es.bvalero.replacer.replacement.type;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.ReplacementFindApi;
import es.bvalero.replacer.finder.StandardType;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
class ReplacementTypeFindService implements ReplacementTypeFindApi {

    // Dependency injection
    private final ReplacementFindApi replacementFindApi;

    ReplacementTypeFindService(ReplacementFindApi replacementFindApi) {
        this.replacementFindApi = replacementFindApi;
    }

    @Override
    public Optional<StandardType> findReplacementType(
        WikipediaLanguage lang,
        String replacement,
        boolean caseSensitive
    ) {
        return this.replacementFindApi.findReplacementType(lang, replacement, caseSensitive);
    }
}
