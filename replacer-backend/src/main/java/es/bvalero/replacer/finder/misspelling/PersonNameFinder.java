package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.ImmutableFinderPriority;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import org.springframework.stereotype.Component;

/**
 * Find person names which are used also as nouns and thus are false positives,
 * e. g. in Spanish `Julio` in `Julio Verne`, as "julio" is also the name of a month
 * to be written in lowercase.
 */
@Component
public class PersonNameFinder implements ImmutableFinder {
    private static final Collection<String> PERSON_NAMES = Arrays.asList(
        "Domingo",
        "Frances",
        "Julio",
        "Los Angeles",
        "Manchester",
        "Sidney",
        "Sky"
    );

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
        List<Immutable> results = new ArrayList<>(100);

        // We loop over all the words and find them in the text with the indexOf function
        for (String word : PERSON_NAMES) {
            int start = 0;
            while (start >= 0) {
                start = text.indexOf(word, start);
                if (start >= 0) { // Word found
                    if (isWordFollowedByUppercase(start, word, text)) {
                        results.add(Immutable.of(start, word, this));
                    }
                    start += word.length();
                }
            }
        }

        return results;
    }

    private boolean isWordFollowedByUppercase(int start, String word, String text) {
        int upperCasePos = start + word.length() + 1;
        return (
            upperCasePos < text.length() &&
            FinderUtils.isWordCompleteInText(start, word, text) &&
            Character.isUpperCase(text.charAt(upperCasePos))
        );
    }
}
