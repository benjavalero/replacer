package es.bvalero.replacer.finder.replacement.finders;

import static org.apache.commons.lang3.StringUtils.SPACE;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find a century and replace it with the template
 */
@Component
public class CenturyFinder implements ReplacementFinder {

    private static final String CENTURY_WORD = "siglo";
    private static final String CENTURY_SEARCH = CENTURY_WORD.substring(1);
    private static final Set<Character> CENTURY_LETTERS = Set.of('I', 'i', 'V', 'v', 'X', 'x');
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

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        if (WikipediaLanguage.SPANISH == page.getId().getLang()) {
            return LinearMatchFinder.find(page, this::findCentury);
        } else {
            return Collections.emptyList();
        }
    }

    private int findCentury(WikipediaPage page, int start, List<MatchResult> matches) {
        final String text = page.getContent();
        int startCentury = findStartCentury(text, start);
        if (startCentury >= 0) {
            int endCentury = startCentury + CENTURY_WORD.length();
            String century = findCenturyWord(text, startCentury);
            if (century == null) {
                return endCentury;
            }

            // Check the century number
            final String centuryNumber = findCenturyNumber(text, endCentury);
            if (centuryNumber == null) {
                return endCentury;
            } else {
                endCentury += 1 + centuryNumber.length();
                century += (SPACE + centuryNumber);
            }

            // Check the era
            final String era = findEraAfter(text, century, startCentury);
            endCentury += era.length();
            century += era;

            // Check the link
            Boolean isLinked = isLinked(text, century, startCentury);
            if (isLinked == null) {
                return endCentury;
            } else if (isLinked) {
                startCentury -= 2;
                endCentury += 2;
                century = "[[" + century + "]]";
            }

            matches.add(LinearMatchResult.of(startCentury, century));
            return endCentury;
        } else {
            return -1;
        }
    }

    private int findStartCentury(String text, int start) {
        final int pos = text.indexOf(CENTURY_SEARCH, start);
        return pos >= 1 ? pos - 1 : -1;
    }

    @Nullable
    private String findCenturyWord(String text, int start) {
        final char firstLetter = text.charAt(start);
        final int endCentury = start + CENTURY_WORD.length();
        if ((firstLetter == 'S' || firstLetter == 's') && text.charAt(endCentury) == ' ') {
            return firstLetter + CENTURY_SEARCH;
        } else {
            return null;
        }
    }

    @Nullable
    private String findCenturyNumber(String text, int endCentury) {
        final MatchResult centuryNumber = FinderUtils.findWordAfter(text, endCentury);
        if (centuryNumber == null) {
            return null;
        } else {
            final String number = centuryNumber.group();
            return isCenturyNumber(number) ? number : null;
        }
    }

    private boolean isCenturyNumber(String text) {
        return text.chars().mapToObj(c -> (char) c).allMatch(this::isCenturyLetter);
    }

    private boolean isCenturyLetter(char ch) {
        return CENTURY_LETTERS.contains(ch);
    }

    // Return, if exists, the era after the century, including the space between; or an empty string.
    private String findEraAfter(String text, String century, int start) {
        int endCentury = start + century.length();
        final String postCentury = text.substring(endCentury, Math.min(endCentury + 6, text.length())); // 6 = &nbsp; length
        String eraSpace = FinderUtils.SPACES.stream().filter(postCentury::startsWith).findAny().orElse(null);
        if (eraSpace != null) {
            endCentury += eraSpace.length();
            final String eraText = text.substring(endCentury, Math.min(endCentury + 10, text.length())); // 10 is the maximum length of an era
            String era = ERA_WORDS.stream().filter(eraText::startsWith).findAny().orElse(null);
            if (era != null) {
                return eraSpace + era;
            }
        }
        return "";
    }

    // True if linked; False if not linked; Null if linked not closed or open.
    @Nullable
    private Boolean isLinked(String text, String century, int start) {
        int endCentury = start + century.length();
        int startLink = Math.max(0, start - 2);
        int endLink = Math.min(text.length(), endCentury + 2);
        final String leftLink = text.substring(startLink, start);
        final String rightLink = text.substring(endCentury, endLink);
        if ("[[".equals(leftLink) && "]]".equals(rightLink)) {
            return true;
        } else if ("[[".equals(leftLink) || "]]".equals(rightLink)) {
            return null;
        } else {
            return false;
        }
    }

    @Override
    public Replacement convert(MatchResult match, WikipediaPage page) {
        final String century = match.group();
        String normalized = century;
        for (String space : FinderUtils.SPACES) {
            if (!SPACE.equals(space)) {
                normalized = normalized.replace(space, " ");
            }
        }
        boolean linked = false;
        if (century.startsWith("[[")) {
            linked = true;
            normalized = normalized.substring(2, normalized.length() - 2);
        }
        final String[] tokens = FinderUtils.splitAsArray(normalized);
        final String centuryWord = tokens[0]; // We keep the original case for the template name to improve the edition diff
        final String centuryNumber = FinderUtils.toUpperCase(tokens[1]);
        String era = tokens.length >= 3 ? tokens[2].substring(0, 1) : "";

        final String templateUpperLink = "{{" + centuryWord + "|" + centuryNumber + "|" + era + "|S|1}}";
        final String templateUpperNoLink = "{{" + centuryWord + "|" + centuryNumber + "|" + era + "|S}}";
        final String templateLowerLink = "{{" + centuryWord + "|" + centuryNumber + "|" + era + "|s|1}}";
        final String templateLowerNoLink = "{{" + centuryWord + "|" + centuryNumber + "|" + era + "|s}}";

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
            .text(century)
            .suggestions(suggestions)
            .build();
    }
}
