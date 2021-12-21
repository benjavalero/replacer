package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class ReplacementValidationService {

    @Autowired
    private ReplacementFinderService replacementFinderService;

    Optional<ReplacementType> findMatchingReplacementType(
        WikipediaLanguage lang,
        String replacement,
        boolean caseSensitive
    ) {
        return replacementFinderService.findMatchingReplacementType(lang, replacement, caseSensitive);
    }
}
