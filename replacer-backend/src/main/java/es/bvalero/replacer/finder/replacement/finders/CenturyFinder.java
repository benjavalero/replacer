package es.bvalero.replacer.finder.replacement.finders;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.roman.code.ConvertToArabic;
import com.roman.code.exception.ConversionException;
import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find a century and replace it with the template
 */
@Component
public class CenturyFinder implements ReplacementFinder {

    private static final String CENTURY_WORD = "siglo";
    private static final String CENTURY_SEARCH = CENTURY_WORD.substring(1);
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
    private static final int MAX_SPACE_LENGTH = FinderUtils.SPACES
        .stream()
        .map(String::length)
        .max(Comparator.comparingInt(o -> o))
        .orElse(0);
    private static final int MAX_ERA_LENGTH = ERA_WORDS
        .stream()
        .map(String::length)
        .max(Comparator.comparingInt(o -> o))
        .orElse(0);

    private static final String REGEX_CENTURY = String.format(
        "[%s]+",
        CENTURY_LETTERS.stream().map(String::valueOf).collect(Collectors.joining())
    );
    private static final RunAutomaton AUTOMATON_CENTURY = new RunAutomaton(new RegExp(REGEX_CENTURY).toAutomaton());

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        // The linear approach is 20x better than using a regex
        if (WikipediaLanguage.SPANISH == page.getId().getLang()) {
            return LinearMatchFinder.find(page, this::findCentury);
        } else {
            return Collections.emptyList();
        }
    }

    @Nullable
    private MatchResult findCentury(WikipediaPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            int startCentury = findStartCentury(text, start);
            if (startCentury < 0) {
                break;
            }

            int endCentury = startCentury + CENTURY_WORD.length();
            final String centuryWord = findCenturyWord(text, startCentury);
            if (centuryWord == null) {
                start = endCentury + 1;
                continue;
            }

            // Check the century number
            final LinearMatchResult centuryNumber = findCenturyNumber(text, endCentury);
            if (centuryNumber == null) {
                start = endCentury + 1;
                continue;
            } else {
                endCentury = centuryNumber.end();
            }

            // Check the era
            final LinearMatchResult era = findEra(text, endCentury);
            if (era != null) {
                endCentury = era.end();
            }

            // Check the link
            final Boolean isLinked = isLinked(text, startCentury, endCentury);
            if (isLinked == null) {
                start = endCentury + 1;
                continue;
            } else if (isLinked) {
                startCentury -= 2;
                endCentury += 2;
            }

            final LinearMatchResult match = LinearMatchResult.of(
                startCentury,
                text.substring(startCentury, endCentury)
            );
            match.addGroup(LinearMatchResult.of(startCentury, centuryWord)); // The start is not relevant
            match.addGroup(centuryNumber);
            if (era != null) {
                match.addGroup(era);
            }
            return match;
        }
        return null;
    }

    private int findStartCentury(String text, int start) {
        final int pos = text.indexOf(CENTURY_SEARCH, start);
        return pos >= 1 ? pos - 1 : -1;
    }

    @Nullable
    private String findCenturyWord(String text, int start) {
        final char firstLetter = text.charAt(start);
        final int endCentury = start + CENTURY_WORD.length();
        if ((firstLetter == 'S' || firstLetter == 's') && Character.isWhitespace(text.charAt(endCentury))) {
            return firstLetter + CENTURY_SEARCH;
        } else {
            return null;
        }
    }

    @Nullable
    private LinearMatchResult findCenturyNumber(String text, int endCentury) {
        final LinearMatchResult centuryNumber = FinderUtils.findWordAfter(text, endCentury);
        // The century word is followed by a white-space, but we need to check the chars in the middle.
        if (centuryNumber == null || centuryNumber.start() != endCentury + 1) {
            return null;
        } else {
            return isCenturyNumber(centuryNumber.group()) ? centuryNumber : null;
        }
    }

    private boolean isCenturyNumber(String text) {
        try {
            return ConvertToArabic.fromRoman(FinderUtils.toUpperCase(text)) <= 21;
        } catch (ConversionException ce) {
            return false;
        }
    }

    @Nullable
    private LinearMatchResult findEra(String text, int endCenturyNumber) {
        final String postCentury = text.substring(
            endCenturyNumber,
            Math.min(endCenturyNumber + MAX_SPACE_LENGTH, text.length())
        );
        final String eraSpace = FinderUtils.SPACES.stream().filter(postCentury::startsWith).findAny().orElse(null);
        if (eraSpace != null) {
            final int startEra = endCenturyNumber + eraSpace.length();
            final String eraText = text.substring(startEra, Math.min(startEra + MAX_ERA_LENGTH, text.length()));
            final String era = ERA_WORDS.stream().filter(eraText::startsWith).findAny().orElse(null);
            if (era != null) {
                return LinearMatchResult.of(startEra, era);
            }
        }
        return null;
    }

    // True if linked; False if not linked; Null if linked not closed or open.
    @Nullable
    private Boolean isLinked(String text, int start, int end) {
        final int startLink = Math.max(0, start - 2);
        final int endLink = Math.min(text.length(), end + 2);
        final String leftLink = text.substring(startLink, start);
        final String rightLink = text.substring(end, endLink);
        if ("[[".equals(leftLink) && "]]".equals(rightLink)) {
            return true;
        } else if ("[[".equals(leftLink) || "]]".equals(rightLink)) {
            return null;
        } else {
            return false;
        }
    }

    @Override
    public Replacement convert(MatchResult matchResult, WikipediaPage page) {
        final String text = page.getContent();
        final LinearMatchResult match = (LinearMatchResult) matchResult;

        String centuryText = match.group();
        final String centuryWord = match.group(0);
        final String centuryNumber = FinderUtils.toUpperCase(match.group(1));
        final String era = match.groupCount() == 3 ? match.group(2).substring(0, 1) : EMPTY;
        final boolean linked = centuryText.startsWith("[[");

        // Try to fix simple centuries close to this one
        String extension = "";
        final LinearMatchResult matchNext = findNextCentury(text, match.end(), centuryNumber);
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
        final boolean uppercase = FinderUtils.startsWithUpperCase(centuryWord);
        if (uppercase) {
            suggestions.add(Suggestion.of(templateUpperNoLink, "siglo en versalitas; con mayúscula; sin enlazar"));
            if (linked) {
                suggestions.add(
                    Suggestion.of(templateUpperLink, "siglo en versalitas; con mayúscula; enlazado —no recomendado—")
                );
            }
        }
        suggestions.add(Suggestion.of(templateLowerNoLink, "siglo en versalitas; con minúscula; sin enlazar"));
        if (linked) {
            suggestions.add(
                Suggestion.of(templateLowerLink, "siglo en versalitas; con minúscula; enlazado —no recomendado—")
            );
        }

        return Replacement
            .builder()
            .type(ReplacementType.CENTURY)
            .start(match.start())
            .text(centuryText)
            .suggestions(suggestions)
            .build();
    }

    @Nullable
    private LinearMatchResult findNextCentury(String text, int endCentury, String centuryNumber) {
        final AutomatonMatcher matcherNext = AUTOMATON_CENTURY.newMatcher(text, endCentury, text.length());
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
            if (FinderUtils.toLowerCase(text.substring(endCentury, startNext)).contains(CENTURY_WORD)) {
                return null;
            }

            // Check the next century number is greater than the previous one
            if (ConvertToArabic.fromRoman(centuryNext) <= ConvertToArabic.fromRoman(centuryNumber)) {
                return null;
            }

            return LinearMatchResult.of(startNext, centuryNext);
        }
        return null;
    }

    private String fixSimpleCentury(String century) {
        return "{{Siglo|" + century + "}}";
    }
}
