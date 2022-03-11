package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Optional;
import java.util.regex.MatchResult;

public interface ReplacementFinder extends Finder<Replacement> {
    @Override
    default boolean validate(MatchResult match, WikipediaPage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }

    default Optional<ReplacementType> findMatchingReplacementType(
        WikipediaLanguage lang,
        String replacement,
        boolean caseSensitive
    ) {
        return Optional.empty();
    }
}
