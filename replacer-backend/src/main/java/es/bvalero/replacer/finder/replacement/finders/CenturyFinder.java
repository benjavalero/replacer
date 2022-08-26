package es.bvalero.replacer.finder.replacement.finders;

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

    private static final String NON_BREAKING_SPACE = "&nbsp;";
    private static final Set<Character> CENTURY_LETTERS = Set.of('I', 'i', 'V', 'v', 'X', 'x');
    private static final List<String> ERA_WORDS = List.of(
        "aC",
        "a.C.",
        "a. C.",
        "a.&nbsp;C.",
        "dC",
        "d.C.",
        "d. C.",
        "d.&nbsp;C."
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
        int startCentury = text.indexOf("iglo", start) - 1;
        if (startCentury >= 0) {
            int endCentury = startCentury + "siglo".length();
            final char firstLetter = text.charAt(startCentury);
            if ((firstLetter != 'S' && firstLetter != 's') || text.charAt(endCentury) != ' ') {
                return endCentury;
            }
            String century = firstLetter + "iglo";

            // Check the century number
            final String centuryNumber = FinderUtils.findWordAfter(text, endCentury);
            if (centuryNumber == null) {
                return endCentury;
            } else if (isCenturyNumber(centuryNumber)) {
                endCentury += 1 + centuryNumber.length();
                century += (" " + centuryNumber);
            } else {
                return endCentury + 1 + centuryNumber.length();
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

    private boolean isCenturyNumber(String text) {
        return text.chars().mapToObj(c -> (char) c).allMatch(this::isCenturyLetter);
    }

    private boolean isCenturyLetter(char ch) {
        return CENTURY_LETTERS.contains(ch);
    }

    // Return, if exists, the era after the century, including the space between; or an empty string.
    private String findEraAfter(String text, String century, int start) {
        int endCentury = start + century.length();
        String eraSpace = null;
        final String postCentury = text.substring(endCentury, Math.min(endCentury + 6, text.length())); // 6 = &nbsp; length
        if (postCentury.startsWith(" ")) {
            eraSpace = " ";
        } else if (postCentury.startsWith(NON_BREAKING_SPACE)) {
            eraSpace = NON_BREAKING_SPACE;
        }
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
        String normalized = century.replace(NON_BREAKING_SPACE, " ");
        boolean linked = false;
        if (century.startsWith("[[")) {
            linked = true;
            normalized = normalized.substring(2, normalized.length() - 2);
        }
        final String[] tokens = normalized.split(" ");
        final String centuryWord = tokens[0]; // We keep the original case for the template name to improve the edition diff
        final String centuryNumber = FinderUtils.toUpperCase(tokens[1]);
        String era = tokens.length >= 3 ? tokens[2].substring(0, 1) : "";

        final String templateUpperLink = String.format("{{%s|%s|%s|S|1}}", centuryWord, centuryNumber, era);
        final String templateUpperNoLink = String.format("{{%s|%s|%s|S}}", centuryWord, centuryNumber, era);
        final String templateLowerLink = String.format("{{%s|%s|%s|s|1}}", centuryWord, centuryNumber, era);
        final String templateLowerNoLink = String.format("{{%s|%s|%s|s}}", centuryWord, centuryNumber, era);

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
