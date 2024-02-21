package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.END_LINK;
import static es.bvalero.replacer.finder.util.FinderUtils.START_LINK;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.roman.code.ConvertToArabic;
import com.roman.code.exception.ConversionException;
import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find a century and replace it with the template
 */
@Component
class CenturyFinder implements ReplacementFinder {

    private static final String CENTURY_WORD = "siglo";
    private static final String CENTURY_SEARCH = CENTURY_WORD.substring(1);
    private static final char SPACE = ' ';
    private static final Set<Character> CENTURY_LETTERS = Set.of('I', 'V', 'X');
    private static final List<String> ERA_WORDS = List.of(
        "aC",
        "a.C.",
        "a. C.",
        "a.&nbsp;C.",
        "a.{{esd}}C.",
        "dC",
        "d.C.",
        "d. C.",
        "d.&nbsp;C.",
        "d.{{esd}}C."
    );

    private static final String REGEX_CENTURY_LETTERS = String.format(
        "[%s]+",
        CENTURY_LETTERS.stream().map(String::valueOf).collect(Collectors.joining())
    );
    private static final RunAutomaton AUTOMATON_CENTURY_LETTERS = new RunAutomaton(
        new RegExp(REGEX_CENTURY_LETTERS).toAutomaton()
    );
    private static final String REGEX_ERA_WORDS = String.format(
        "(%s)",
        FinderUtils.joinAlternate(
            ERA_WORDS
                .stream()
                .map(s -> s.replace(".", "\\."))
                .map(s -> s.replace("{", "\\{"))
                .map(s -> s.replace("}", "\\}"))
                .map(s -> s.replace("&", "\\&"))
                .toList()
        )
    );
    private static final RunAutomaton AUTOMATON_ERA_WORDS = new RunAutomaton(new RegExp(REGEX_ERA_WORDS).toAutomaton());

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // The linear approach is 20x better than using a regex
        if (WikipediaLanguage.SPANISH == page.getPageKey().getLang()) {
            return LinearMatchFinder.find(page, this::findCentury);
        } else {
            return List.of();
        }
    }

    @Nullable
    private MatchResult findCentury(FinderPage page, int start) {
        final String text = page.getContent();
        // TODO: Reduce cyclomatic complexity
        while (start >= 0 && start < text.length()) {
            MatchResult centuryWord = findCenturyWord(text, start);
            if (centuryWord == null) {
                return null;
            }
            int startCentury = centuryWord.start();
            int endCentury = centuryWord.end();

            // Check the century number
            assert text.charAt(endCentury) == SPACE;
            final MatchResult centuryNumber = findCenturyNumber(text, endCentury + 1);
            if (centuryNumber == null) {
                start = endCentury + 1;
                continue;
            } else {
                endCentury = centuryNumber.end();
            }

            // Check the era (optional)
            final MatchResult era = findEra(text, endCentury);
            if (era != null) {
                endCentury = era.end();
            }

            // Check the link
            final Boolean isLinked = isLinked(text, startCentury, endCentury);
            if (isLinked == null) {
                start = endCentury + 1;
                continue;
            } else if (isLinked) {
                startCentury -= START_LINK.length();
                endCentury += END_LINK.length();
            }

            final FinderMatchResult match = FinderMatchResult.of(text, startCentury, endCentury);
            match.addGroup(centuryWord);
            match.addGroup(centuryNumber);
            match.addGroup(Objects.requireNonNullElseGet(era, FinderMatchResult::ofEmpty));
            return match;
        }
        return null;
    }

    @Nullable
    private MatchResult findCenturyWord(String text, int start) {
        while (start >= 0 && start < text.length()) {
            // Instead of making a case-insensitive search, we find the end of the word,
            // and then we check the word is complete. It is a little faster than finding the word with both cases.
            final int startSuffix = text.indexOf(CENTURY_SEARCH, start);
            if (startSuffix <= 0) { // <= 0 to be able to get the character before
                return null;
            }

            final int startCentury = startSuffix - 1;
            final char firstLetter = text.charAt(startCentury);
            final String centuryWord = firstLetter + CENTURY_SEARCH;
            final int endCentury = startCentury + centuryWord.length();
            // We only consider the word complete and followed by a whitespace
            if (
                (firstLetter == 'S' || firstLetter == 's') &&
                (FinderUtils.isWordCompleteInText(startCentury, centuryWord, text)) &&
                text.charAt(endCentury) == SPACE
            ) {
                return FinderMatchResult.of(startCentury, centuryWord);
            } else {
                // Keep on searching
                start = endCentury;
            }
        }
        return null;
    }

    @Nullable
    private MatchResult findCenturyNumber(String text, int start) {
        final MatchResult centuryNumber = FinderUtils.findWordAfter(text, start);
        if (centuryNumber == null || centuryNumber.start() != start || !isCenturyNumber(centuryNumber.group())) {
            return null;
        }
        return centuryNumber;
    }

    private boolean isCenturyNumber(String text) {
        try {
            return ConvertToArabic.fromRoman(FinderUtils.toUpperCase(text)) <= 21;
        } catch (ConversionException ce) {
            return false;
        }
    }

    @Nullable
    private MatchResult findEra(String text, int start) {
        // We could limit the end of the search, but it is not worth to complicate the code.
        final AutomatonMatcher matcherEra = AUTOMATON_ERA_WORDS.newMatcher(text, start, text.length());
        if (matcherEra.find()) {
            final int startEra = start + matcherEra.start();
            final String eraSpace = text.substring(start, startEra);
            if (FinderUtils.isActualSpace(eraSpace)) {
                return FinderMatchResult.of(startEra, matcherEra.group());
            }
        }
        return null;
    }

    /** True if linked; False if not linked; Null if linked not closed or open. */
    @Nullable
    private Boolean isLinked(String text, int start, int end) {
        final int startLink = Math.max(0, start - START_LINK.length());
        final int endLink = Math.min(text.length(), end + END_LINK.length());
        final String leftLink = text.substring(startLink, start);
        final String rightLink = text.substring(end, endLink);
        if (START_LINK.equals(leftLink) && END_LINK.equals(rightLink)) {
            return true;
        } else if (START_LINK.equals(leftLink) || END_LINK.equals(rightLink)) {
            return null;
        } else {
            return false;
        }
    }

    @Override
    public Replacement convert(MatchResult match, FinderPage page) {
        final String text = page.getContent();

        String centuryText = match.group();
        final String centuryWord = match.group(1);
        final String centuryNumber = FinderUtils.toUpperCase(match.group(2));
        final String era = match.group(3).isEmpty() ? EMPTY : match.group(3).substring(0, 1);
        final boolean linked = centuryText.startsWith(START_LINK);

        // Try to fix simple centuries close to this one
        String extension = EMPTY;
        final MatchResult matchNext = findNextCentury(text, match.end(), centuryNumber);
        if (matchNext != null) {
            centuryText = text.substring(match.start(), matchNext.end());
            extension = text.substring(match.end(), matchNext.start()) + fixSimpleCentury(matchNext.group());
        }

        final String templateUpperLink = "{{" + centuryWord + "|" + centuryNumber + "|" + era + "|S|1}}" + extension;
        final String templateUpperNoLink = "{{" + centuryWord + "|" + centuryNumber + "|" + era + "|S}}" + extension;
        final String templateLowerLink = "{{" + centuryWord + "|" + centuryNumber + "|" + era + "|s|1}}" + extension;
        final String templateLowerNoLink = "{{" + centuryWord + "|" + centuryNumber + "|" + era + "|s}}" + extension;

        final List<Suggestion> suggestions = new ArrayList<>(4);
        // Not linked centuries are recommended
        // Offer always the lowercase alternative
        final String linkedComment = "enlazado —solo para temas relacionados con el calendario—";
        final boolean uppercase = FinderUtils.startsWithUpperCase(centuryWord);
        if (uppercase) {
            suggestions.add(Suggestion.of(templateUpperNoLink, "siglo en versalitas; con mayúscula; sin enlazar"));
            if (linked) {
                suggestions.add(
                    Suggestion.of(templateUpperLink, "siglo en versalitas; con mayúscula; " + linkedComment)
                );
            }
        }
        suggestions.add(Suggestion.of(templateLowerNoLink, "siglo en versalitas; con minúscula; sin enlazar"));
        if (linked) {
            suggestions.add(Suggestion.of(templateLowerLink, "siglo en versalitas; con minúscula; " + linkedComment));
        }

        return Replacement
            .builder()
            .page(page)
            .type(StandardType.CENTURY)
            .start(match.start())
            .text(centuryText)
            .suggestions(suggestions)
            .build();
    }

    @Nullable
    private MatchResult findNextCentury(String text, int endCentury, String centuryNumber) {
        final AutomatonMatcher matcherNext = AUTOMATON_CENTURY_LETTERS.newMatcher(text, endCentury, text.length());
        if (matcherNext.find()) {
            final int startNext = endCentury + matcherNext.start();
            final String centuryNext = matcherNext.group();
            // Check the found century number is actually a century
            if (!isCenturyNumber(centuryNext) || !FinderUtils.isWordCompleteInText(startNext, centuryNext, text)) {
                return null;
            }

            // Check there are no more than 3 words between both century numbers
            if (FinderUtils.countWords(text, endCentury, startNext) > 3) {
                return null;
            }

            // Check we are not capturing a complete century again
            if (text.substring(endCentury, startNext).contains(CENTURY_SEARCH)) {
                return null;
            }

            // Check the next century number is greater than the previous one
            if (ConvertToArabic.fromRoman(centuryNext) <= ConvertToArabic.fromRoman(centuryNumber)) {
                return null;
            }

            return FinderMatchResult.of(startNext, centuryNext);
        }
        return null;
    }

    private String fixSimpleCentury(String century) {
        return "{{Siglo|" + century + "}}";
    }
}
