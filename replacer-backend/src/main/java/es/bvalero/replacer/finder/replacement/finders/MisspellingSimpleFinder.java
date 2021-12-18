package es.bvalero.replacer.finder.replacement.finders;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import lombok.Setter;
import org.apache.commons.collections4.SetValuedMap;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find misspellings with only word, e.g. `habia` in Spanish
 */
@Component
public class MisspellingSimpleFinder extends MisspellingFinder implements PropertyChangeListener {

    @Setter(onMethod_ = @TestOnly)
    @Autowired
    private SimpleMisspellingLoader simpleMisspellingLoader;

    @PostConstruct
    public void init() {
        simpleMisspellingLoader.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        this.buildMisspellingMaps((SetValuedMap<WikipediaLanguage, Misspelling>) evt.getNewValue());
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // There are thousands of simple misspellings
        // The best approach is to find all words in the text and check if they are in the list
        return LinearMatchFinder.find(page, this::findResult);
    }

    @Nullable
    private MatchResult findResult(FinderPage page, int start) {
        final List<MatchResult> matches = new ArrayList<>();
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findWord(page.getContent(), start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findWord(String text, int start, List<MatchResult> matches) {
        // Find next letter
        int startWord = -1;
        for (int i = start; i < text.length(); i++) {
            if (isLetter(text.charAt(i))) {
                startWord = i;
                break;
            }
        }

        if (startWord >= 0) {
            // Find complete word
            for (int j = startWord + 1; j < text.length(); j++) {
                if (!isLetter(text.charAt(j))) {
                    final String word = text.substring(startWord, j);
                    matches.add(LinearMatchResult.of(startWord, word));
                    return j;
                }
            }

            // In case of getting here the text ends with a word
            final String word = text.substring(startWord);
            matches.add(LinearMatchResult.of(startWord, word));
        }
        return -1;
    }

    private boolean isLetter(char ch) {
        return Character.isLetter(ch);
    }

    @Override
    ReplacementKind getType() {
        return ReplacementKind.MISSPELLING_SIMPLE;
    }
}
