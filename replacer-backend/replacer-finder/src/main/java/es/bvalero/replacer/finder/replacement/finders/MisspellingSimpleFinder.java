package es.bvalero.replacer.finder.replacement.finders;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.SetValuedMap;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find misspellings with only word, e.g. `habia` in Spanish
 */
@Component
public class MisspellingSimpleFinder extends MisspellingFinder implements PropertyChangeListener {

    // Dependency injection
    private final SimpleMisspellingLoader simpleMisspellingLoader;

    public MisspellingSimpleFinder(SimpleMisspellingLoader simpleMisspellingLoader) {
        this.simpleMisspellingLoader = simpleMisspellingLoader;
    }

    @PostConstruct
    public void init() {
        simpleMisspellingLoader.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        buildMisspellingMaps((SetValuedMap<WikipediaLanguage, StandardMisspelling>) evt.getNewValue());
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // There are thousands of simple misspellings
        // The best approach with big difference is to find all words in the text and check if they are in the list
        // Within this approach the linear finder gives the best performance
        return LinearMatchFinder.find(page, this::findWord);
    }

    @Nullable
    private MatchResult findWord(FinderPage page, int start) {
        final WikipediaLanguage lang = page.getPageKey().getLang();
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startWord = findStartWord(text, start);
            if (startWord >= 0) {
                final int endWord = findEndWord(text, startWord);
                final String word = text.substring(startWord, endWord);
                if (isValid(word, startWord, text, lang)) {
                    return FinderMatchResult.of(startWord, word);
                } else {
                    // The char after the word is a non-letter, so we can start searching the next word one position after.
                    start = endWord + 1;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    private int findStartWord(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (isLetter(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private int findEndWord(String text, int startWord) {
        for (int i = startWord + 1; i < text.length(); i++) {
            if (!isLetter(text.charAt(i))) {
                return i;
            }
        }
        return text.length();
    }

    private boolean isLetter(char ch) {
        return Character.isLetter(ch);
    }

    private boolean isValid(String word, int startWord, String text, WikipediaLanguage lang) {
        // The word is wrapped by non-letters, so we still need to validate the separators.
        // We discard some words in URLs by checking if they are wrapped by a dot or a slash.
        return (
            (isExistingWord(word, lang) && FinderUtils.isWordCompleteInText(startWord, word, text)) &&
            !FinderUtils.isUrlWord(startWord, word, text)
        );
    }

    @Override
    ReplacementKind getType() {
        return ReplacementKind.SIMPLE;
    }
}
