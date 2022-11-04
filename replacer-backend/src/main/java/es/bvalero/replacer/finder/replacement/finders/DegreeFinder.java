package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.NON_BREAKING_SPACE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.Suggestion;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find temperature degrees with the wrong symbol
 */
@Component
public class DegreeFinder implements ReplacementFinder {

    private static final char DEGREE = '\u00b0'; // °
    private static final char MASCULINE_ORDINAL = '\u00ba'; // º
    private static final Set<Character> DEGREE_LETTERS = Set.of('C', 'F');

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        // The performance is about 5x better than an automaton approach
        return LinearMatchFinder.find(page, this::findDegree);
    }

    @Nullable
    private MatchResult findDegree(WikipediaPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final LinearMatchResult matchSymbol = findDegreeSymbol(text, start);
            if (matchSymbol == null) {
                break;
            }

            final int startSymbol = matchSymbol.start();
            final LinearMatchResult matchLetter = findDegreeLetter(text, matchSymbol.end());
            if (matchLetter == null) {
                start = startSymbol + 1;
                continue;
            }

            final String space2 = text.substring(matchSymbol.end(), matchLetter.start());
            if (StringUtils.isNotBlank(space2)) {
                start = startSymbol + 1;
                continue;
            }

            final int endDegree = matchLetter.end();

            final LinearMatchResult matchBefore = FinderUtils.findWordBefore(text, startSymbol);
            if (matchBefore == null) {
                // This would only happen if the degree is at the very start of the content but we need to check it
                start = startSymbol + 1;
                continue;
            }

            final String word = matchBefore.group();
            final String space1 = text.substring(matchBefore.end(), startSymbol);
            assert matchSymbol.group().length() == 1;
            final char symbol = matchSymbol.group().charAt(0);
            if (isValidDegree(word, space1, symbol, space2)) {
                start = endDegree;
                continue;
            }
            // If preceded by number the space must be valid
            if (StringUtils.isNumeric(word) && !FinderUtils.isSpace(space1)) {
                start = endDegree;
                continue;
            }

            final LinearMatchResult match = LinearMatchResult.of(
                matchBefore.start(),
                text.substring(matchBefore.start(), endDegree)
            );
            match.addGroup(matchBefore);
            match.addGroup(LinearMatchResult.of(matchBefore.end(), space1));
            match.addGroup(matchLetter);
            return match;
        }
        return null;
    }

    @Nullable
    private LinearMatchResult findDegreeSymbol(String text, int start) {
        final String textSearchable = text.substring(start);
        int startSymbol = StringUtils.indexOfAny(textSearchable, DEGREE, MASCULINE_ORDINAL);
        return startSymbol < 0
            ? null
            : LinearMatchResult.of(start + startSymbol, String.valueOf(textSearchable.charAt(startSymbol)));
    }

    @Nullable
    private LinearMatchResult findDegreeLetter(String text, int startSymbol) {
        final LinearMatchResult matchAfter = FinderUtils.findWordAfter(text, startSymbol);
        if (
            matchAfter == null ||
            matchAfter.group().length() != 1 ||
            !DEGREE_LETTERS.contains(matchAfter.group().charAt(0))
        ) {
            return null;
        } else {
            return matchAfter;
        }
    }

    // A degree is valid if it contains a space and the symbol is correct
    private boolean isValidDegree(String word, String space1, char symbol, String space2) {
        // Only check previous space if the word is a number
        if (StringUtils.isNumeric(word)) {
            return FinderUtils.isActualSpace(space1) && symbol == DEGREE && EMPTY.equals(space2);
        } else {
            return symbol == DEGREE && EMPTY.equals(space2);
        }
    }

    @Override
    public Replacement convert(MatchResult matchResult, WikipediaPage page) {
        LinearMatchResult match = (LinearMatchResult) matchResult;

        final String fixedDegree;
        final String word = match.group(0);
        if (StringUtils.isNumeric(word)) {
            fixedDegree = match.group(0) + NON_BREAKING_SPACE + "°" + match.group(2);
        } else {
            fixedDegree = match.group(0) + match.group(1) + "°" + match.group(2);
        }

        return Replacement
            .builder()
            .type(ReplacementType.DEGREES)
            .start(match.start())
            .text(match.group())
            .suggestions(List.of(Suggestion.ofNoComment(fixedDegree)))
            .build();
    }
}
