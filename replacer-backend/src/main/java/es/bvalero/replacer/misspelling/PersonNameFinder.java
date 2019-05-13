package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class PersonNameFinder implements IgnoredReplacementFinder {

    private final static Collection<String> PERSON_NAMES = Arrays.asList("Domingo", "Frances", "Julio", "Sidney");

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        List<MatchResult> results = new ArrayList<>(100);

        // We loop over all the words and find them in the text with the indexOf function
        for (String word : PERSON_NAMES) {
            int start = 0;
            while (start >= 0) {
                start = text.indexOf(word, start);
                if (start >= 0) {
                    if (isWordFollowedByUppercase(start, word, text)) {
                        results.add(new MatchResult(start, word));
                    }
                    start++;
                }
            }
        }

        return results;
    }

    private boolean isWordFollowedByUppercase(int start, String word, String text) {
        int end = start + word.length();
        return end + 1 < text.length()
                && !Character.isLetter(text.charAt(end))
                && Character.isUpperCase(text.charAt(end + 1));
    }

}
