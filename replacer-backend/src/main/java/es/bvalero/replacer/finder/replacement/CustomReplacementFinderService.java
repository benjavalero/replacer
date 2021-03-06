package es.bvalero.replacer.finder.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.listing.Misspelling;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomReplacementFinderService extends ReplacementFinderService {

    @Autowired
    private MisspellingSimpleFinder misspellingSimpleFinder;

    @Autowired
    private MisspellingComposedFinder misspellingComposedFinder;

    @Override
    public Iterable<Replacement> find(FinderPage page) {
        throw new IllegalCallerException();
    }

    @Loggable(prepend = true, value = Loggable.TRACE, unit = TimeUnit.SECONDS)
    public Iterable<Replacement> findCustomReplacements(FinderPage page, CustomOptions customOptions) {
        CustomReplacementFinder finder = CustomReplacementFinder.of(customOptions);
        return findReplacements(page, Collections.singletonList(finder));
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
