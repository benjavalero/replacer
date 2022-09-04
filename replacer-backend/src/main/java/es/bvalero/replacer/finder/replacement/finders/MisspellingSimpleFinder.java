package es.bvalero.replacer.finder.replacement.finders;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import lombok.Setter;
import org.apache.commons.collections4.SetValuedMap;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        // There are thousands of simple misspellings
        // The best approach is to find all words in the text and check if they are in the list
        return LinearMatchFinder.find(page, this::findWord);
    }

    private int findWord(WikipediaPage page, int start, List<MatchResult> matches) {
        final WikipediaLanguage lang = page.getId().getLang();
        final String text = page.getContent();

        final int startWord = findStartWord(text, start);
        if (startWord >= 0) {
            final int endWord = findEndWord(text, startWord);
            final String word = text.substring(startWord, endWord);
            // Validate first that the word is complete to improve performance
            // The word is wrapped by non-letters, so we still need to validate the separators.
            if (isExistingWord(word, lang) && FinderUtils.isWordCompleteInText(startWord, word, text)) {
                matches.add(LinearMatchResult.of(startWord, word));
            }
            // The char after the word is a non-letter, so we can start searching the next word one position after.
            return endWord + 1;
        } else {
            return -1;
        }
    }

    private int findStartWord(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (isLetter(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private int findEndWord(String text, int start) {
        for (int j = start + 1; j < text.length(); j++) {
            if (!isLetter(text.charAt(j))) {
                return j;
            }
        }
        return text.length();
    }

    private boolean isLetter(char ch) {
        return Character.isLetter(ch);
    }

    @Override
    ReplacementKind getType() {
        return ReplacementKind.SIMPLE;
    }
}
