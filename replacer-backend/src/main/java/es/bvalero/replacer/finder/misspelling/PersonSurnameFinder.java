package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Find person surnames. Also usual nouns preceded by a word starting in uppercase,
 * e.g. `RCA Records`
 */
@Component
public class PersonSurnameFinder implements ImmutableFinder {
    private static final Collection<String> SURNAMES = Arrays.asList("Domingo", "Records", "Sky");

    @Override
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
        List<Immutable> results = new ArrayList<>(100);

        // We loop over all the words and find them in the text with the indexOf function
        for (String word : SURNAMES) {
            int start = 0;
            while (start >= 0) {
                start = text.indexOf(word, start);
                if (start >= 0) { // Word found
                    if (isWordPrecededByUppercase(start, word, text)) {
                        results.add(Immutable.of(start, word, this));
                    }
                    start += word.length();
                }
            }
        }

        return results;
    }

    private boolean isWordPrecededByUppercase(int start, String word, String text) {
        if (FinderUtils.isWordCompleteInText(start, word, text) && start >= 2) {
            char lastLetter = text.charAt(start - 1);
            for (int i = start - 2; i >= 0; i--) {
                char ch = text.charAt(i);
                if (Character.isLetter(ch)) {
                    lastLetter = ch;
                } else {
                    return Character.isUpperCase(lastLetter);
                }
            }
        }
        return false;
    }
}
