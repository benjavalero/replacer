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
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        // There are thousands of simple misspellings
        // The best approach is to find all words in the text and check if they are in the list
        return LinearMatchFinder.find(page, this::findWord);
    }

    @Nullable
    private MatchResult findWord(WikipediaPage page, int start) {
        final WikipediaLanguage lang = page.getId().getLang();
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startWord = findStartWord(text, start);
            if (startWord >= 0) {
                final int endWord = findEndWord(text, startWord);
                final String word = text.substring(startWord, endWord);
                if (isValid(word, startWord, text, lang)) {
                    return LinearMatchResult.of(startWord, word);
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
        // Validate first that the word is complete to improve performance
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
