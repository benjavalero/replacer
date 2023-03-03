package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderService;
import es.bvalero.replacer.finder.Replacement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
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

    @PostConstruct
    public void sortReplacementFinders() {
        Collections.sort(this.replacementFinders);
    }

    @Override
    public Iterable<Finder<Replacement>> getFinders() {
        return new ArrayList<>(this.replacementFinders);
    }

    public Optional<StandardType> findMatchingReplacementType(
        WikipediaLanguage lang,
        String replacement,
        boolean caseSensitive
    ) {
        return this.replacementFinders.stream()
            .map(finder -> finder.findMatchingReplacementType(lang, replacement, caseSensitive))
            .filter(Optional::isPresent)
            .findAny()
            .orElse(Optional.empty());
    }
}
