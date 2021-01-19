package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.LinearIterable;
import es.bvalero.replacer.finder.LinearMatcher;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.page.IndexablePage;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.collections4.SetValuedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find misspellings with only word, e.g. `habia` in Spanish
 */
@Component
public class MisspellingSimpleFinder extends MisspellingFinder {

    static final String TYPE_MISSPELLING_SIMPLE = "Ortografía";

    @Autowired
    private MisspellingManager misspellingManager;

    @Override
    MisspellingManager getMisspellingManager() {
        return misspellingManager;
    }

    @Override
    void processMisspellingChange(SetValuedMap<WikipediaLanguage, Misspelling> misspellings) {
        // Do nothing
    }

    @Override
    public Iterable<Replacement> find(IndexablePage page) {
        // We need to perform additional transformations according to the language
        return StreamSupport
            .stream(new LinearIterable<>(page, this::findResult, this::convertMatch).spliterator(), false)
            .filter(r -> isExistingWord(r.getText(), page.getLang()))
            .filter(r -> FinderUtils.isWordCompleteInText(r.getStart(), r.getText(), page.getContent()))
            .map(r -> r.withSubtype(getSubtype(r.getText(), page.getLang())))
            .map(r -> r.withSuggestions(findSuggestions(r.getText(), page.getLang())))
            .collect(Collectors.toList());
    }

    @Nullable
    private MatchResult findResult(IndexablePage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
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
                    String word = text.substring(startWord, j);
                    matches.add(LinearMatcher.of(startWord, word));
                    return j;
                }
            }

            // In case of getting here the text ends with a word
            String word = text.substring(startWord);
            matches.add(LinearMatcher.of(startWord, word));
        }
        return -1;
    }

    private boolean isLetter(char ch) {
        return Character.isLetter(ch);
    }

    @Override
    public String getType() {
        return TYPE_MISSPELLING_SIMPLE;
    }
}
