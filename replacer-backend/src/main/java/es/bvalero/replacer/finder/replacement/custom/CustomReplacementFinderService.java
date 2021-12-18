package es.bvalero.replacer.finder.replacement.custom;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderService;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.finders.MisspellingComposedFinder;
import es.bvalero.replacer.finder.replacement.finders.MisspellingSimpleFinder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomReplacementFinderService implements FinderService<Replacement> {

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

    public Iterable<Replacement> findCustomReplacements(FinderPage page, CustomOptions customOptions) {
        final CustomReplacementFinder finder = CustomReplacementFinder.of(customOptions);
        return findIterable(page, Collections.singleton(finder));
    }

    /** Checks if the given word exists as a misspelling and in this case returns the type */
    public Optional<Misspelling> findExistingMisspelling(String word, WikipediaLanguage lang) {
        final Optional<Misspelling> simpleMisspelling = misspellingSimpleFinder.findMisspellingByWord(word, lang);
        if (simpleMisspelling.isPresent()) {
            return simpleMisspelling;
        } else {
            return misspellingComposedFinder.findMisspellingByWord(word, lang);
        }
    }
}
