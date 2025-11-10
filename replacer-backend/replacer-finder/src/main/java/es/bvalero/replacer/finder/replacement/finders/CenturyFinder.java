package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;
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
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchCollectionFinder;
import es.bvalero.replacer.finder.util.SimpleMatchResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SequencedCollection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import javax.naming.OperationNotSupportedException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find a century and replace it with the template,
 * e.g. <code>siglo XX</code> ==> <code>{{siglo|XX||s}}</code>
 */
@Component
class CenturyFinder implements ReplacementFinder {

    private static final String CENTURY_WORD = "Siglo";
    private static final String CENTURY_SEARCH = CENTURY_WORD.substring(1);
    private static final String[] CENTURY_WORDS = new String[] { "s.", "S.", CENTURY_SEARCH };
    private static final int MAX_WORDS_BETWEEN_CENTURIES = 5;

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
            MatchResult centuryWord = findCenturyWord(text, start);
            if (centuryWord == null) {
                return Collections.emptyList();
            }

            int startCentury = centuryWord.start();
            int endCentury = centuryWord.end();

            // 2. Find century number
            // Ignore numbers immediately after the century word
            final CenturyNumber centuryNumber = findCenturyNumber(text, endCentury);
            if (centuryNumber == null || centuryNumber.start() == endCentury) {
                start = endCentury;
                continue;
            }
            endCentury = centuryNumber.end();

            // Find if the century is wrapped so the positions must be modified
            final MatchResult wrap = findCenturyWrapper(text, startCentury, endCentury, centuryNumber.group());
            if (wrap == null) {
                start = endCentury;
                continue;
            }
            startCentury = wrap.start();
            endCentury = wrap.end();

            // 4. Find the extensions (if any)
            final SequencedCollection<CenturyMatch> extensions = findExtensions(text, endCentury, centuryNumber);

            // If the century word is plural then the extension is mandatory
            // If the century word is plural then we don't consider the century word as part of the replacement
            if (isPluralWord(centuryWord.group())) {
                if (extensions.isEmpty()) {
                    start = endCentury;
                    continue;
                }
                centuryWord = null;
                startCentury = centuryNumber.start();
            }

            // 5. Return the sequence of matches
            final SequencedCollection<MatchResult> matches = new ArrayList<>(extensions.size() + 1);
            matches.add(
                new CenturyMatch(startCentury, text.substring(startCentury, endCentury), centuryWord, centuryNumber)
            );
            matches.addAll(extensions);
            return matches;
        }
        return Collections.emptyList();
    }

    private record CenturyMatch(int start, String group, @Nullable MatchResult word, CenturyNumber number)
        implements SimpleMatchResult {
        static CenturyMatch ofNumber(CenturyNumber number) {
            return new CenturyMatch(number.start(), number.group(), null, number);
        }
    }

    //region Century Word

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
            final MatchResult match = FinderUtils.indexOfAny(text, start, CENTURY_WORDS);
            if (match == null) {
                return null;
            }

            int startCentury = match.start();
            int endCentury = match.end();
            final boolean isAbbreviation = isAbbreviation(match.group());
            if (!isAbbreviation) {
                // Check first letter of the word
                if (startCentury <= 0 || !isCenturyFirstLetter(text.charAt(startCentury - 1))) {
                    start = endCentury;
                    continue;
                }
                startCentury--;

                // Century word can be plural, e.g. "siglos"
                if (isPluralWord(text.charAt(endCentury))) {
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

    private boolean isCenturyFirstLetter(char ch) {
        return ch == 'S' || ch == 's';
    }

    private boolean isPluralWord(char ch) {
        return ch == 's';
    }

    //endregion

    //region Century Number

    private record CenturyNumber(int start, String group, String roman, int arabic, @Nullable String era)
        implements SimpleMatchResult {
        private boolean isEraBefore() {
            return "a".equals(getEraLetter());
        }

        String getEraLetter() {
            return era == null ? EMPTY : ReplacerUtils.toLowerCase(String.valueOf(era.charAt(0)));
        }

        boolean isGreaterThan(CenturyNumber m) {
            if (this.isEraBefore()) {
                return m.arabic() > this.arabic();
            } else if (m.isEraBefore()) {
                return true;
            } else {
                return this.arabic() > m.arabic();
            }
        }
    }

    /**
     * Find a century number with an optional era, e.g. <code>XX d. C.</code> or <code>16</code>
     * The 1st nested group is empty as there is no century word.
     * The century number is stored in the 2nd nested match group.
     * Trick: the Arabic and Roman values are stored in the start and text of the 3rd nested match group.
     * The era, if existing, is stored in the 4th nested match group.
     */
    @Nullable
    private CenturyNumber findCenturyNumber(String text, int start) {
        final MatchResult firstWord = FinderUtils.findWordAfterSpace(text, start);
        if (firstWord == null) {
            return null;
        }
        final CenturyNumber centuryNumber = getCenturyNumber(firstWord);
        if (centuryNumber == null) {
            return null;
        }

        // Find century era (optional)
        final MatchResult era = findEra(text, centuryNumber.end());
        if (era != null) {
            return new CenturyNumber(
                centuryNumber.start(),
                text.substring(centuryNumber.start(), era.end()),
                centuryNumber.roman(),
                centuryNumber.arabic(),
                era.group()
            );
        } else {
            return centuryNumber;
        }
    }

    /** Convert into a century number, null if there is any issue. */
    @Nullable
    private CenturyNumber getCenturyNumber(MatchResult match) {
        final String word = match.group();
        try {
            final int arabic;
            final String roman;
            if (isRomanLetters(word)) {
                roman = ReplacerUtils.toUpperCase(word);
                arabic = ConvertToArabic.fromRoman(roman);
                if (isValidArabicCentury(arabic)) {
                    return new CenturyNumber(match.start(), word, roman, arabic, null);
                }
            } else if (word.length() <= 2 && FinderUtils.isNumber(word)) {
                arabic = Integer.parseInt(word);
                if (isValidArabicCentury(arabic)) {
                    roman = ConvertToRoman.fromArabic(arabic);
                    return new CenturyNumber(match.start(), word, roman, arabic, null);
                }
            }
        } catch (ConversionException | NumberFormatException | OperationNotSupportedException ignored) {}
        return null;
    }

    private boolean isRomanLetters(String word) {
        // 5 is the maximum length of a Roman century: XVIII
        if (word.length() > 5) {
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
        int endEra = startEra + 1;
        if (endEra >= text.length() || !isEraFirstLetter(text.charAt(startEra))) {
            return null;
        }
        if (text.charAt(endEra) == DOT) {
            endEra++;
        }
        // Find the second part of the era: "C" with an optional preposition before and/or dot at the end
        MatchResult secondWord = FinderUtils.findWordAfterSpace(text, endEra);
        if (secondWord != null && isEraPreposition(secondWord.group())) {
            secondWord = FinderUtils.findWordAfterSpace(text, secondWord.end());
        }
        if (secondWord == null || !isEraEndLetters(secondWord.group())) {
            return null;
        }
        endEra = secondWord.end();
        if (endEra < text.length() && text.charAt(endEra) == DOT) {
            endEra++;
        }
        return FinderMatchRange.of(text, startEra, endEra);
    }

    private boolean isEraFirstLetter(char ch) {
        return ch == 'a' || ch == 'd' || ch == 'A' || ch == 'D';
    }

    private boolean isEraPreposition(String word) {
        return "de".equals(word);
    }

    private boolean isEraEndLetters(String word) {
        return "c".equals(word) || "C".equals(word) || "dC".equals(word) || "dc".equals(word);
    }

    //endregion

    //region Century Wrapper

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

    //endregion

    //region Century Extensions

    /** Find the existing extensions to the century, i.e. more century numbers close to the found century. */
    private SequencedCollection<CenturyMatch> findExtensions(String text, int start, CenturyNumber currentNumber) {
        final SequencedCollection<CenturyMatch> extensions = new ArrayList<>(2);
        CenturyMatch extension = findExtension(text, start, currentNumber);
        while (extension != null) {
            extensions.add(extension);
            extension = findExtension(text, extension.end(), extension.number());
        }
        return extensions;
    }

    /** Find a valid century number close enough */
    @Nullable
    private CenturyMatch findExtension(String text, int start, CenturyNumber currentNumber) {
        // Find the next century number with at most certain number of words between both century numbers
        int numWordsFound = 0;
        int wordStart = start;
        while (numWordsFound <= MAX_WORDS_BETWEEN_CENTURIES) {
            final MatchResult nextWord = FinderUtils.findWordAfter(text, wordStart);
            if (nextWord == null || text.indexOf('\n', wordStart, nextWord.start()) >= 0) {
                return null;
            }

            // Check if it is a century number and is greater than the previous one
            // We "find" instead of "get" to capture a possible era in the extension
            final CenturyNumber nextNumber = findCenturyNumber(text, nextWord.start());
            if (nextNumber != null && nextNumber.isGreaterThan(currentNumber)) {
                // Check we are not capturing a complete century again
                for (String centuryWord : CENTURY_WORDS) {
                    if (text.indexOf(centuryWord, start, nextNumber.start()) >= 0) {
                        return null;
                    }
                }

                return CenturyMatch.ofNumber(nextNumber);
            } else {
                numWordsFound++;
                wordStart = nextWord.end();
            }
        }
        return null;
    }

    private boolean isPluralWord(String word) {
        return isPluralWord(word.charAt(word.length() - 1));
    }

    //endregion

    //region Century Convert

    @Override
    public Replacement convertWithNoSuggestions(MatchResult match, FinderPage page) {
        return Replacement.ofNoSuggestions(match.start(), match.group(), StandardType.CENTURY);
    }

    @Override
    public Replacement convert(MatchResult matchResult, FinderPage page) {
        assert matchResult instanceof CenturyMatch;
        final CenturyMatch match = (CenturyMatch) matchResult;
        if (match.word() == null) {
            return convertCenturyNumber(match);
        } else {
            return convertCenturyWithWord(match, page);
        }
    }

    private Replacement convertCenturyWithWord(CenturyMatch match, FinderPage page) {
        final String text = page.getContent();

        assert match.word() != null;
        final String centuryWord = match.word().group();
        final CenturyNumber centuryNumber = match.number();
        final String romanNumber = centuryNumber.roman();
        String eraLetter;
        if (centuryNumber.era() != null) {
            final String eraText = centuryNumber.era();
            eraLetter = ReplacerUtils.toLowerCase(String.valueOf(eraText.charAt(0)));
        } else {
            // Special case when the century is wrapped by an era template
            final int startCenturyWord = match.word().start();
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
        final boolean isLinkAliased = isLinked && text.charAt(centuryNumber.end()) == PIPE;
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
            if (isLinked) {
                fixedCentury = buildCenturyTemplate(templateName, romanNumber, eraLetter, upperPrefix, true);
                suggestions.add(Suggestion.of(fixedCentury, buildSuggestionComment(upperPrefix, true)));
            }
            fixedCentury = buildCenturyTemplate(templateName, romanNumber, eraLetter, upperPrefix, false);
            suggestions.add(Suggestion.of(fixedCentury, buildSuggestionComment(upperPrefix, false)));
        }
        if (isLinked) {
            fixedCentury = buildCenturyTemplate(templateName, romanNumber, eraLetter, lowerPrefix, true);
            suggestions.add(Suggestion.of(fixedCentury, buildSuggestionComment(lowerPrefix, true)));
        }
        fixedCentury = buildCenturyTemplate(templateName, romanNumber, eraLetter, lowerPrefix, false);
        suggestions.add(Suggestion.of(fixedCentury, buildSuggestionComment(lowerPrefix, false)));

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

    private Replacement convertCenturyNumber(CenturyMatch match) {
        final CenturyNumber number = match.number();
        final String romanNumber = number.roman();
        final String eraLetter = number.era() != null
            ? ReplacerUtils.toLowerCase(String.valueOf(number.era().charAt(0)))
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
    //endregion
}
