package es.bvalero.replacer.finder.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.page.IndexablePage;
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
    public Iterable<Replacement> find(IndexablePage page) {
        throw new IllegalCallerException();
    }

    @Loggable(prepend = true, value = Loggable.TRACE, unit = TimeUnit.SECONDS)
    public Iterable<Replacement> findCustomReplacements(IndexablePage page, CustomOptions customOptions) {
        CustomReplacementFinder finder = CustomReplacementFinder.of(customOptions);
        return findReplacements(page, Collections.singletonList(finder));
    }

    /** Checks if the given word exists as a misspelling and in this case returns the type */
    public Optional<String> findExistingMisspelling(String word, WikipediaLanguage lang) {
        if (misspellingSimpleFinder.findMisspellingByWord(word, lang).isPresent()) {
            return Optional.of(misspellingSimpleFinder.getType());
        } else if (misspellingComposedFinder.findMisspellingByWord(word, lang).isPresent()) {
            return Optional.of(misspellingComposedFinder.getType());
        } else {
            return Optional.empty();
        }
    }
}
