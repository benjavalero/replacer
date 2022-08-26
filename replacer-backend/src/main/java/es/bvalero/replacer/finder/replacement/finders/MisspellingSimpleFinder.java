package es.bvalero.replacer.finder.replacement.finders;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
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
        final String text = page.getContent();

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
        return ReplacementKind.SIMPLE;
    }

    @Override
    public boolean validate(MatchResult match, WikipediaPage page) {
        // For simple misspellings, we also need to check it the word is a misspelling.
        return isExistingWord(match.group(), page.getId().getLang()) && super.validate(match, page);
    }
}
