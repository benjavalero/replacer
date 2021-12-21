package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Independent service that finds all the replacements in a given text.
 * It is composed by several specific replacement finders: misspelling, date format, etc.
 * which implement the same interface.
 * The service applies all these specific finders and returns the collected results.
 */
@Service
public class ReplacementFinderService implements FinderService<Replacement> {

    @Autowired
    private List<ReplacementFinder> replacementFinders;

    @Override
    public Iterable<Finder<Replacement>> getFinders() {
        return new ArrayList<>(replacementFinders);
    }

    public Optional<ReplacementType> findMatchingReplacementType(
        WikipediaLanguage lang,
        String replacement,
        boolean caseSensitive
    ) {
        return replacementFinders
            .stream()
            .map(finder -> finder.findMatchingReplacementType(lang, replacement, caseSensitive))
            .filter(Optional::isPresent)
            .findAny()
            .orElse(Optional.empty());
    }
}
