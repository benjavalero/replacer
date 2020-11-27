package es.bvalero.replacer.finder.composed;

import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.finder.misspelling.MisspellingComposedFinder;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find character "ó" between numbers, e.g. `2 ó 3`
 */
@Component
public class AcuteOFinder implements ReplacementFinder {
    static final String SUBTYPE_ACUTE_O_NUMBERS = "ó entre números";
    static final String SUBTYPE_ACUTE_O_WORDS = "ó entre palabras";
    static final String SEARCH_ACUTE_O = " ó ";
    static final String ACUTE_O = "ó";
    static final String FIX_ACUTE_O = "o";

    @Override
    public Iterable<Replacement> find(String text, WikipediaLanguage lang) {
        if (WikipediaLanguage.SPANISH == lang) {
            return new LinearIterable<>(text, this::findResult, this::convert);
        } else {
            return Collections.emptyList();
        }
    }

    @Nullable
    public MatchResult findResult(String text, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && matches.isEmpty()) {
            start = findAcuteO(text, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findAcuteO(String text, int start, List<MatchResult> matches) {
        int startAcuteO = text.indexOf(SEARCH_ACUTE_O, start);
        if (startAcuteO >= 0) {
            int endAcute0 = startAcuteO + SEARCH_ACUTE_O.length();
            String wordBefore = findWordBefore(text, startAcuteO);
            String wordAfter = findWordAfter(text, endAcute0);
            if (wordBefore == null || wordAfter == null) {
                return endAcute0;
            } else {
                matches.add(
                    LinearMatcher.of(startAcuteO - wordBefore.length(), wordBefore + SEARCH_ACUTE_O + wordAfter)
                );
                return startAcuteO + SEARCH_ACUTE_O.length();
            }
        } else {
            return -1;
        }
    }

    private Replacement convert(MatchResult match) {
        return Replacement
            .builder()
            .type(MisspellingComposedFinder.TYPE_MISSPELLING_COMPOSED)
            .subtype(findSubtype(match.group()))
            .start(match.group().indexOf(SEARCH_ACUTE_O) + 1)
            .text(ACUTE_O)
            .suggestions(findSuggestions())
            .build();
    }

    private String findSubtype(String text) {
        int pos = text.indexOf(SEARCH_ACUTE_O);
        return (
                FinderUtils.isNumber(text.substring(0, pos)) &&
                FinderUtils.isNumber(text.substring(pos + SEARCH_ACUTE_O.length()))
            )
            ? SUBTYPE_ACUTE_O_NUMBERS
            : SUBTYPE_ACUTE_O_WORDS;
    }

    private List<Suggestion> findSuggestions() {
        return Collections.singletonList(Suggestion.ofNoComment(FIX_ACUTE_O));
    }

    @Nullable
    private String findWordAfter(String text, int start) {
        int end;
        for (end = start; end < text.length(); end++) {
            char ch = text.charAt(end);
            if (!Character.isLetterOrDigit(ch) && !Character.isSpaceChar(ch)) {
                break;
            }
        }
        String word = text.substring(start, end);
        return StringUtils.isBlank(word) ? null : word.trim();
    }

    @Nullable
    private String findWordBefore(String text, int start) {
        int end;
        for (end = start; end >= 0; end--) {
            char ch = text.charAt(end);
            if (!Character.isLetterOrDigit(ch) && !Character.isSpaceChar(ch)) {
                break;
            }
        }
        String word = text.substring(Math.max(0, end), start);
        return StringUtils.isBlank(word) ? null : word.trim();
    }
}
