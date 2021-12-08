package es.bvalero.replacer.finder.replacement.custom;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPageMapper;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.replacement.ImmutableFilterFinderService;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.finders.MisspellingComposedFinder;
import es.bvalero.replacer.finder.replacement.finders.MisspellingSimpleFinder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;

@Service
public class CustomReplacementFinderService extends ImmutableFilterFinderService<Replacement> {

    @Autowired
    private MisspellingSimpleFinder misspellingSimpleFinder;

    @Autowired
    private MisspellingComposedFinder misspellingComposedFinder;

    @Override
    public List<Replacement> find(FinderPage page) {
        throw new IllegalCallerException();
    }

    @Override
    public Iterable<Replacement> findIterable(FinderPage page) {
        throw new IllegalCallerException();
    }

    @Override
    public Iterable<Finder<Replacement>> getFinders() {
        throw new IllegalCallerException();
    }

    @Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 1, warnUnit = TimeUnit.SECONDS)
    public Iterable<Replacement> findCustomReplacements(WikipediaPage wikipediaPage, CustomOptions customOptions) {
        CustomReplacementFinder finder = CustomReplacementFinder.of(customOptions);
        FinderPage page = FinderPageMapper.fromDomain(wikipediaPage);
        Iterable<Replacement> allResults = findIterable(page, Collections.singletonList(finder));
        return filterResults(page, allResults);
    }

    /** Checks if the given word exists as a misspelling and in this case returns the type */
    public Optional<Misspelling> findExistingMisspelling(String word, WikipediaLanguage lang) {
        Optional<Misspelling> simpleMisspelling = misspellingSimpleFinder.findMisspellingByWord(word, lang);
        if (simpleMisspelling.isPresent()) {
            return simpleMisspelling;
        } else {
            return misspellingComposedFinder.findMisspellingByWord(word, lang);
        }
    }
}
