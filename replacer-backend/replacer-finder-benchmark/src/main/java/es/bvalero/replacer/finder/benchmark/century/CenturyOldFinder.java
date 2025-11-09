package es.bvalero.replacer.finder.benchmark.century;

import static es.bvalero.replacer.finder.util.FinderUtils.*;
import static es.bvalero.replacer.finder.util.FinderUtils.END_TEMPLATE;
import static es.bvalero.replacer.finder.util.FinderUtils.NON_BREAKING_SPACE_TEMPLATE_NAME;
import static es.bvalero.replacer.finder.util.FinderUtils.PIPE;
import static es.bvalero.replacer.finder.util.FinderUtils.START_TEMPLATE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.roman.code.ConvertToArabic;
import com.roman.code.ConvertToRoman;
import com.roman.code.exception.ConversionException;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchCollectionFinder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SequencedCollection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import javax.naming.OperationNotSupportedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

class CenturyOldFinder implements ReplacementFinder {

    private static final String CENTURY_WORD = "Siglo";
    private static final String CENTURY_SEARCH = CENTURY_WORD.substring(1);
    private static final int MAX_WORDS_BETWEEN_CENTURIES = 4;

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        if (WikipediaLanguage.SPANISH == page.getPageKey().getLang()) {
            return LinearMatchCollectionFinder.find(page, this::findCentury);
        } else {
            return Stream.of();
        }
    }

    private SequencedCollection<MatchResult> findCentury(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            // 1. Find century word
            final MatchResult centuryWord = findCenturyWord(text, start);
            if (centuryWord == null) {
                return Collections.emptyList();
            }

            int startCentury = centuryWord.start();
            int endCentury = centuryWord.end();

            // 2. Find century number
            final MatchResult centuryNumber = findCenturyNumber(text, endCentury);
            if (centuryNumber == null) {
                start = endCentury;
                continue;
            } else {
                endCentury = centuryNumber.end();
            }

            // Find if the century is wrapped so the positions must be modified
            final MatchResult wrap = findCenturyWrapper(text, startCentury, endCentury, centuryNumber.group());
            if (wrap == null) {
                start = endCentury;
                continue;
            } else {
                startCentury = wrap.start();
                endCentury = wrap.end();
            }

            // 4. Find the extensions (if any)
            final SequencedCollection<MatchResult> extensions = findExtensions(
                text,
                endCentury,
                centuryNumber.start(3)
            );

            // If the century word is plural then the extension is mandatory
            final boolean isPlural = isPlural(centuryWord.group());
            if (isPlural && extensions.isEmpty()) {
                start = endCentury;
                continue;
            }

            // 5. Return the sequence of matches
            final SequencedCollection<MatchResult> matches = new ArrayList<>(extensions.size() + 1);
            if (isPlural) {
                // If the century word is plural then we don't consider the century word as part of the replacement
                matches.add(centuryNumber);
            } else {
                final FinderMatchRange match = FinderMatchRange.ofNested(text, startCentury, endCentury);
                match.addGroup(centuryWord);
                match.addGroup(centuryNumber);
                matches.add(match);
            }
            matches.addAll(extensions);
            return matches;
        }
        return Collections.emptyList();
    }

    /**
     * Find a century word, e.g. <code>siglo</code>
     * It may start with uppercase or be plural: <code>Siglo</code>, <code>siglos</code>
     * Or be an abbreviation: <code>s.</code>
     */
    @Nullable
    private MatchResult findCenturyWord(String text, int start) {
        while (start >= 0 && start < text.length()) {
            // Find any of the supported century words, finding first the most common.
            // Instead of finding all the variants of "siglo", we find the root "iglo" and then check the variants.
            final MatchResult match = FinderUtils.indexOfAny(text, start, "s.", "S.", CENTURY_SEARCH);
            if (match == null) {
                return null;
            }

            int startCentury = match.start();
            int endCentury = match.end();
            final boolean isAbbreviation = isAbbreviation(match.group());
            if (!isAbbreviation) {
                // Check first letter of the word
                if (startCentury <= 0) {
                    start = endCentury;
                    continue;
                }
                final char firstLetter = text.charAt(startCentury - 1);
                if (firstLetter != 'S' && firstLetter != 's') {
                    start = endCentury;
                    continue;
                }
                startCentury--;

                // Century word can be plural, e.g. "siglos"
                if (isPlural(text.charAt(endCentury))) {
                    endCentury++;
                }
            }

            // We only consider the word complete
            if (FinderUtils.isWordCompleteInText(startCentury, endCentury, text)) {
                return isAbbreviation ? match : FinderMatchRange.of(text, startCentury, endCentury);
            }

            start = endCentury;
        }
        return null;
    }

    private boolean isAbbreviation(String word) {
        return word.length() == 2;
    }

    /**
     * Find a century number with an optional era, e.g. <code>XX d. C.</code> or <code>16</code>
     * The 1st nested group is empty as there is no century word.
     * The century number is stored in the 2nd nested match group.
     * Trick: the Arabic and Roman values are stored in the start and text of the 3rd nested match group.
     * The era, if existing, is stored in the 4th nested match group.
     */
    @Nullable
    private MatchResult findCenturyNumber(String text, int start) {
        final MatchResult centuryNumber = FinderUtils.findWordAfterSpace(text, start);
        if (centuryNumber == null) {
            return null;
        }
        final String centuryNumberStr = centuryNumber.group();
        final MatchResult centuryNumberTrick = getCenturyNumber(centuryNumberStr);
        if (centuryNumberTrick == null) {
            return null;
        }

        // Find century era (optional)
        int endCenturyNumber = centuryNumber.end();
        final MatchResult era = findEra(text, centuryNumber.end());
        if (era != null) {
            endCenturyNumber = era.end();
        }

        final FinderMatchRange match = FinderMatchRange.ofNested(text, centuryNumber.start(), endCenturyNumber);
        match.addGroup(FinderMatchRange.ofEmpty(text, centuryNumber.start()));
        match.addGroup(centuryNumber);
        match.addGroup(centuryNumberTrick);
        if (era != null) {
            match.addGroup(era);
        }

        return match;
    }

    /**
     * Get a fake match result representing a century number.
     * The start contains the Arabic value and the group the Roman value.
     */
    @Nullable
    private MatchResult getCenturyNumber(String word) {
        try {
            int arabic = -1;
            String roman = null;
            if (isRomanLetters(word)) {
                roman = ReplacerUtils.toUpperCase(word);
                arabic = ConvertToArabic.fromRoman(roman);
            } else if (word.length() <= 2 && FinderUtils.isNumber(word)) {
                arabic = Integer.parseInt(word);
            }
            if (isValidArabicCentury(arabic)) {
                if (roman == null) {
                    roman = ConvertToRoman.fromArabic(arabic);
                }
                return FinderMatchResult.of(arabic, roman);
            }
        } catch (ConversionException | NumberFormatException | OperationNotSupportedException ignored) {}
        return null;
    }

    private boolean isRomanLetters(String word) {
        if (word.length() > 5) {
            // 5 is the maximum length of a Roman century: XVIII
            return false;
        }
        for (int i = 0; i < word.length(); i++) {
            if (!isCenturyLetter(word.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isCenturyLetter(char ch) {
        return ch == 'I' || ch == 'V' || ch == 'X' || ch == 'i' || ch == 'v' || ch == 'x';
    }

    private boolean isValidArabicCentury(int number) {
        return number > 0 && number <= 21;
    }

    /** Find a century era, e.g. <code>a. C.</code> */
    @Nullable
    private MatchResult findEra(String text, int start) {
        final MatchResult firstWord = FinderUtils.findWordAfterSpace(text, start);
        if (firstWord == null) {
            return null;
        }
        // Find the first letter of the era: "a" or "d" with an optional dot at the end
        final int startEra = firstWord.start();
        final char firstLetter = text.charAt(startEra);
        if (firstLetter != 'a' && firstLetter != 'd' && firstLetter != 'A' && firstLetter != 'D') {
            return null;
        }
        int endEra = startEra + 1;
        if (endEra >= text.length()) {
            return null;
        }
        if (text.charAt(endEra) == DOT) {
            endEra++;
        }
        // Find the second part of the era: "C" with an optional preposition before and/or dot at the end
        MatchResult secondWord = FinderUtils.findWordAfterSpace(text, endEra);
        if (secondWord != null && "de".equals(secondWord.group())) {
            secondWord = FinderUtils.findWordAfterSpace(text, secondWord.end());
        }
        if (secondWord == null) {
            return null;
        }
        if (
            !"c".equals(secondWord.group()) &&
            !"C".equals(secondWord.group()) &&
            !"dC".equals(secondWord.group()) &&
            !"dc".equals(secondWord.group())
        ) {
            return null;
        }
        endEra = secondWord.end();
        if (endEra < text.length() && text.charAt(endEra) == DOT) {
            endEra++;
        }
        return FinderMatchRange.of(text, startEra, endEra);
    }

    /**
     * Find if the century (word and number) is wrapped by a link or a known template,
     * e.g. <code>[[Siglo XX]]</code> or <code>{{esd|siglo XX}}</code>
     * If there is such a wrap, return the wrap. If not, return the original match.
     * Return null in case of error or a wider wrap to be ignored, e.g. <code>[[Siglo XX en España]]</code>
     */
    @Nullable
    private MatchResult findCenturyWrapper(String text, int start, int end, String centuryNumber) {
        // 1. Check if wrapped by a link, e.g. "[[Siglo XX]]"
        final MatchResult endLink = FinderUtils.indexOfAny(text, end, START_LINK, END_LINK);
        final boolean endsWithLink = endLink != null && END_LINK.equals(endLink.group());
        if (endsWithLink) {
            // Check if the century is followed immediately by the link end
            // Exception: an aliased link, e.g. "[[Siglo XX|XX]]"
            boolean isAliased =
                text.charAt(end) == PIPE &&
                end + 1 + centuryNumber.length() == endLink.start() &&
                ReplacerUtils.containsAtPosition(text, centuryNumber, end + 1);
            if (!isAliased && !FinderUtils.isEmptyBlankOrSpaceAlias(text, end, endLink.start())) {
                return null;
            }

            // Check if the century is preceded immediately by the link start
            final MatchResult startLink = FinderUtils.lastIndexOfAny(text, start, START_LINK, END_LINK);
            final boolean startsWithLink = startLink != null && START_LINK.equals(startLink.group());
            if (startsWithLink) {
                if (!FinderUtils.isEmptyBlankOrSpaceAlias(text, startLink.end(), start)) {
                    return null;
                }

                // At this point the link is valid
                return FinderMatchRange.of(text, startLink.start(), endLink.end());
            }
        }

        // 2. Check if wrapped by a non-breaking space template, e.g. "{{esd|siglo XX}}"
        final String nbsTemplatePrefix = START_TEMPLATE + NON_BREAKING_SPACE_TEMPLATE_NAME + PIPE;
        final int startNbsTemplate = start - nbsTemplatePrefix.length();
        final boolean startsWithNbsTemplate = ReplacerUtils.containsAtPosition(
            text,
            nbsTemplatePrefix,
            startNbsTemplate
        );
        if (startsWithNbsTemplate && ReplacerUtils.containsAtPosition(text, END_TEMPLATE, end)) {
            return FinderMatchRange.of(text, startNbsTemplate, end + END_TEMPLATE.length());
        }

        // 3. Check if wrapped by an era template, e.g. "{{AC|siglo I}}"
        final String eraTemplatePrefix = START_TEMPLATE + "AC" + PIPE;
        final int startEraTemplate = start - eraTemplatePrefix.length();
        final boolean startsWithEraTemplate =
            startEraTemplate >= 0 && eraTemplatePrefix.equalsIgnoreCase(text.substring(startEraTemplate, start));
        if (startsWithEraTemplate && ReplacerUtils.containsAtPosition(text, END_TEMPLATE, end)) {
            return FinderMatchRange.of(text, startEraTemplate, end + END_TEMPLATE.length());
        }

        // If not wrapped, return the original match.
        return FinderMatchRange.of(text, start, end);
    }

    /** Find the existing extensions to the century, i.e. more century numbers close to the found century. */
    private SequencedCollection<MatchResult> findExtensions(String text, int start, int centuryNumber) {
        final SequencedCollection<MatchResult> extensions = new ArrayList<>(2);
        MatchResult extension = findExtension(text, start, centuryNumber);
        while (extension != null) {
            extensions.add(extension);
            extension = findExtension(text, extension.end(), extension.start(3));
        }
        return extensions;
    }

    /** Find a valid century number close enough */
    @Nullable
    private MatchResult findExtension(String text, int start, int centuryNumber) {
        // Find the next century number with at most 3 words between both century numbers
        int numWordsFound = 0;
        int wordStart = start;
        while (numWordsFound <= MAX_WORDS_BETWEEN_CENTURIES) {
            final MatchResult wordFound = FinderUtils.findWordAfter(text, wordStart);
            if (wordFound == null) {
                return null;
            }

            // Check we are not capturing a complete century again
            if (wordFound.group().contains(CENTURY_SEARCH)) {
                return null;
            }

            // Check if it is a century number and is greater than the previous one
            final MatchResult numberMatch = findCenturyNumber(text, wordFound.start());
            if (numberMatch != null && numberMatch.start(3) > centuryNumber) {
                return numberMatch;
            } else {
                numWordsFound++;
                wordStart = wordFound.end();
            }
        }
        return null;
    }

    private boolean isPlural(String word) {
        return isPlural(word.charAt(word.length() - 1));
    }

    private boolean isPlural(char ch) {
        return ch == 's';
    }

    @Override
    public Replacement convertWithNoSuggestions(MatchResult match, FinderPage page) {
        return Replacement.ofNoSuggestions(match.start(), match.group(), StandardType.CENTURY);
    }

    @Override
    public Replacement convert(MatchResult match, FinderPage page) {
        final String centuryWord = match.group(1);
        if (StringUtils.isEmpty(centuryWord)) {
            return convertCenturyNumber(match);
        } else {
            return convertCenturyWithWord(match, page);
        }
    }

    private Replacement convertCenturyWithWord(MatchResult matchResult, FinderPage page) {
        final String text = page.getContent();
        final FinderMatchRange match = (FinderMatchRange) matchResult;

        final String centuryWord = match.group(1);
        assert StringUtils.isNotEmpty(centuryWord);
        final MatchResult centuryNumberMatch = match.getGroup(2);
        final String centuryNumber = centuryNumberMatch.group(3);
        String eraLetter;
        if (centuryNumberMatch.groupCount() > 3) {
            final String eraText = centuryNumberMatch.group(4);
            eraLetter = ReplacerUtils.toLowerCase(String.valueOf(eraText.charAt(0)));
        } else {
            // Special case when the century is wrapped by an era template
            final int startCenturyWord = match.start(1);
            final String eraTemplatePrefix = START_TEMPLATE + "AC" + PIPE;
            if (
                startCenturyWord >= eraTemplatePrefix.length() &&
                eraTemplatePrefix.equalsIgnoreCase(
                    text.substring(startCenturyWord - eraTemplatePrefix.length(), startCenturyWord)
                )
            ) {
                eraLetter = "a";
            } else {
                eraLetter = EMPTY;
            }
        }

        // Check if the century is surrounded by a link
        final boolean isLinked = ReplacerUtils.containsAtPosition(match.group(), START_LINK, 0);
        final boolean isLinkAliased = isLinked && text.charAt(match.end(2)) == PIPE;
        final boolean isAbbreviated = isAbbreviation(centuryWord);
        final String templateName = isAbbreviated ? CENTURY_WORD : centuryWord;
        final String lowerPrefix;
        if (isLinkAliased) {
            lowerPrefix = EMPTY;
        } else if (isAbbreviated) {
            lowerPrefix = "a";
        } else {
            lowerPrefix = "s";
        }
        final String upperPrefix = ReplacerUtils.toUpperCase(lowerPrefix);

        final List<Suggestion> suggestions = new ArrayList<>(4);

        String fixedCentury;
        // Not linked centuries are recommended
        // Offer always the lowercase alternative
        final boolean isUppercase = FinderUtils.startsWithUpperCase(centuryWord) && !isLinkAliased;
        if (isUppercase) {
            fixedCentury = buildCenturyTemplate(templateName, centuryNumber, eraLetter, upperPrefix, false);
            suggestions.add(Suggestion.of(fixedCentury, buildSuggestionComment(upperPrefix, false)));
            if (isLinked) {
                fixedCentury = buildCenturyTemplate(templateName, centuryNumber, eraLetter, upperPrefix, true);
                suggestions.add(Suggestion.of(fixedCentury, buildSuggestionComment(upperPrefix, true)));
            }
        }
        fixedCentury = buildCenturyTemplate(templateName, centuryNumber, eraLetter, lowerPrefix, false);
        suggestions.add(Suggestion.of(fixedCentury, buildSuggestionComment(lowerPrefix, false)));
        if (isLinked) {
            fixedCentury = buildCenturyTemplate(templateName, centuryNumber, eraLetter, lowerPrefix, true);
            suggestions.add(Suggestion.of(fixedCentury, buildSuggestionComment(lowerPrefix, true)));
        }

        return Replacement.of(match.start(), match.group(), StandardType.CENTURY, suggestions);
    }

    private String buildSuggestionComment(String centuryLetter, boolean isLinked) {
        final StringBuilder comment = new StringBuilder("siglo en versalitas");
        if (centuryLetter.equals("S")) {
            comment.append("; con mayúscula");
        } else if (centuryLetter.equals("s")) {
            comment.append("; con minúscula");
        } else if (centuryLetter.equalsIgnoreCase("a")) {
            comment.append("; abreviado");
        }
        comment.append("; ");
        if (isLinked) {
            comment.append("enlazado —solo para temas relacionados con el calendario—");
        } else {
            comment.append("sin enlazar");
        }
        return comment.toString();
    }

    private Replacement convertCenturyNumber(MatchResult match) {
        final String romanNumber = match.group(3);
        final String eraLetter = match.groupCount() > 3
            ? ReplacerUtils.toLowerCase(String.valueOf(match.group(4).charAt(0)))
            : EMPTY;
        final String suggestionText = buildCenturyTemplate(CENTURY_WORD, romanNumber, eraLetter, EMPTY, false);
        final List<Suggestion> suggestions = List.of(Suggestion.of(suggestionText, "siglo en versalitas"));
        return Replacement.of(match.start(), match.group(), StandardType.CENTURY, suggestions);
    }

    private String buildCenturyTemplate(
        String templateName,
        String centuryNumber,
        String eraLetter,
        String centuryLetter,
        boolean linked
    ) {
        final StringBuilder template = new StringBuilder();
        template.append(START_TEMPLATE);
        template.append(templateName);
        template.append(PIPE);
        template.append(centuryNumber);
        template.append(PIPE);
        template.append(eraLetter);
        template.append(PIPE);
        template.append(centuryLetter);
        template.append(PIPE);
        template.append(linked ? "1" : EMPTY);
        // Remove the trailing pipes
        while (template.charAt(template.length() - 1) == PIPE) {
            template.deleteCharAt(template.length() - 1);
        }
        template.append(END_TEMPLATE);
        return template.toString();
    }
}
