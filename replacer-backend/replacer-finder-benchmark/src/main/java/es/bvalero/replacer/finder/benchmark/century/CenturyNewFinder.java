package es.bvalero.replacer.finder.benchmark.century;

import static es.bvalero.replacer.finder.util.FinderUtils.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.roman.code.ConvertToRoman;
import com.roman.code.exception.ConversionException;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.replacement.finders.CenturyMatch;
import es.bvalero.replacer.finder.replacement.finders.CenturyNumber;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchCollectionFinder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import javax.naming.OperationNotSupportedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find a century and replace it with the template,
 * e.g. <code>siglo XX</code> ==> <code>{{siglo|XX||s}}</code>
 */
@Component
class CenturyNewFinder implements ReplacementFinder {

    private static final String CENTURY_WORD = "Siglo";
    private static final String CENTURY_SEARCH = CENTURY_WORD.substring(1);
    private static final String[] CENTURY_WORDS = new String[] { "s.", "S.", CENTURY_SEARCH };
    private static final String[] WRAP_TEMPLATES = new String[] { NON_BREAKING_SPACE_TEMPLATE_NAME, "nowrap" };
    private static final int MAX_WORDS_BETWEEN_CENTURIES = 5;

    static final Map<String, String> ARABIC_TO_ROMAN;
    private static final Map<String, Integer> ROMAN_TO_ARABIC;

    static {
        // Pre-compute conversions between Arabic and Roman numerals (1-21) for performance
        Map<String, String> arabicToRoman = new java.util.HashMap<>();
        for (int i = 1; i <= 21; i++) {
            try {
                arabicToRoman.put(String.valueOf(i), ConvertToRoman.fromArabic(i));
            } catch (OperationNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
        ARABIC_TO_ROMAN = Collections.unmodifiableMap(arabicToRoman);

        Map<String, Integer> romanToArabic = new java.util.HashMap<>();
        for (int i = 1; i <= 21; i++) {
            try {
                String roman = ConvertToRoman.fromArabic(i);
                romanToArabic.put(roman, i);
                romanToArabic.put(ReplacerUtils.toLowerCase(roman), i);
            } catch (OperationNotSupportedException | ConversionException e) {
                throw new RuntimeException(e);
            }
        }
        ROMAN_TO_ARABIC = Collections.unmodifiableMap(romanToArabic);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        if (WikipediaLanguage.SPANISH == page.getPageKey().getLang()) {
            return LinearMatchCollectionFinder.find(page, this::findCenturyMatches);
        } else {
            return Stream.of();
        }
    }

    //region Century Match

    /**
     * Find a complete century occurrence starting from the given position.
     * Returns a collection containing the main century match and any extensions (e.g., "siglos XX-XXI").
     * For plural forms, only the numbers are returned if extensions exist.
     */
    private SequencedCollection<MatchResult> findCenturyMatches(FinderPage page, int start) {
        final String text = page.getContent();
        final CenturyMatch mainMatch = findMainCenturyMatch(text, start);
        if (mainMatch == null) {
            return Collections.emptyList();
        }

        // Find and add the extensions (if any)
        final SequencedCollection<CenturyMatch> extensions = findExtensions(text, mainMatch.end(), mainMatch.number());

        final SequencedCollection<MatchResult> matches = new ArrayList<>(extensions.size() + 1);
        if (mainMatch.isPlural()) {
            if (extensions.isEmpty()) {
                // Plural without extensions: continue searching from the end of this match
                return findCenturyMatches(page, mainMatch.end());
            }
            // For plural forms, return only the numbers (without the word)
            matches.add(CenturyMatch.ofCenturyNumber(mainMatch.number()));
        } else {
            matches.add(mainMatch);
        }
        matches.addAll(extensions);
        return matches;
    }

    /**
     * Find the main century match (word + number + wrapper) starting from the given position.
     * Returns null if no valid century is found.
     */
    @Nullable
    private CenturyMatch findMainCenturyMatch(String text, int start) {
        while (start >= 0 && start < text.length()) {
            // 1. Find century word
            MatchResult centuryWord = findCenturyWord(text, start);
            if (centuryWord == null) {
                return null;
            }

            // 2. Find century number
            // Ignore numbers immediately after the century word
            final CenturyNumber centuryNumber = findCenturyNumber(text, centuryWord.end());
            if (centuryNumber == null || centuryNumber.start() == centuryWord.end()) {
                start = centuryWord.end();
                continue;
            }

            // 3. Find if the century is wrapped and build the final match
            final CenturyMatch centuryMatch = findCenturyWrapper(text, centuryWord, centuryNumber);
            if (centuryMatch == null) {
                start = centuryNumber.end();
                continue;
            }

            return centuryMatch;
        }
        return null;
    }

    //endregion

    //region Century Word

    /**
     * Find a century word, e.g. <code>siglo</code>
     * It may start with uppercase or be plural: <code>Siglo</code>, <code>siglos</code>
     * Or be an abbreviation: <code>s.</code>
     */
    @Nullable
    private MatchResult findCenturyWord(String text, int start) {
        while (start >= 0 && start < text.length()) {
            // Find any of the supported century words.
            // Abbreviations (e.g., "s.") are most common, so we optimize by searching for their components.
            // Instead of searching for all variants, we search for suffixes and validate the prefix later.
            // Character search is faster than string search, so we handle '.' separately.

            // Search for '.' first (most common)
            final int posDot = text.indexOf('.', start);

            // Search for "iglo" only up to the dot position (if found)
            final int rightLimit = posDot >= 0 ? posDot : text.length();
            final int posIglo = text.indexOf("iglo", start, rightLimit);

            // Determine which occurrence comes first
            final int startCentury;
            int endCentury;
            final boolean foundIglo;
            if (posIglo >= 0 && (posDot < 0 || posIglo < posDot)) {
                // "iglo" found (and either no '.' or "iglo" comes before '.')
                startCentury = posIglo - 1;
                endCentury = posIglo + 4;
                foundIglo = true;
            } else if (posDot >= 0) {
                // '.' found (and no "iglo" before it)
                startCentury = posDot - 1;
                endCentury = posDot + 1;
                foundIglo = false;
            } else {
                // No occurrences found
                return null;
            }

            // Check first letter of the word
            if (FinderUtils.toLowerCaseAscii(text.charAt(startCentury)) != 's') {
                start = endCentury;
                continue;
            }

            // If we found "iglo", check for plural suffix "s"
            if (foundIglo && endCentury < text.length() && text.charAt(endCentury) == 's') {
                endCentury++;
            }

            // We only consider the word complete
            if (FinderUtils.isWordCompleteInText(startCentury, endCentury, text)) {
                return FinderMatchRange.of(text, startCentury, endCentury);
            }

            start = endCentury;
        }
        return null;
    }

    //endregion

    //region Century Number

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
        return era != null ? centuryNumber.withEra(text, era) : centuryNumber;
    }

    /** Convert into a century number, null if there is any issue. */
    @Nullable
    private CenturyNumber getCenturyNumber(MatchResult match) {
        final String word = match.group();
        try {
            // Try as Roman numeral first (use pre-computed map)
            Integer arabic = ROMAN_TO_ARABIC.get(word);
            if (arabic != null) {
                return new CenturyNumber(match.start(), word, arabic, null);
            }
            // Try as Arabic numeral (use pre-computed map)
            if (ARABIC_TO_ROMAN.containsKey(word)) {
                return new CenturyNumber(match.start(), word, Integer.parseInt(word), null);
            }
        } catch (NumberFormatException ignored) {}
        return null;
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
        if (text.charAt(endEra) == '.') {
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
        if (endEra < text.length() && text.charAt(endEra) == '.') {
            endEra++;
        }
        return FinderMatchRange.of(text, startEra, endEra);
    }

    private boolean isEraFirstLetter(char ch) {
        final char lower = FinderUtils.toLowerCaseAscii(ch);
        return lower == 'a' || lower == 'd';
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
     * and return the complete CenturyMatch with adjusted positions.
     * Returns null if the wrapper is invalid (e.g., wider link like "[[Siglo XX en España]]").
     */
    @Nullable
    private CenturyMatch findCenturyWrapper(String text, MatchResult centuryWord, CenturyNumber centuryNumber) {
        final int start = centuryWord.start();
        final int end = centuryNumber.end();

        // Try to find a wrapper
        MatchResult wrapper = findLinkWrapper(text, start, end, centuryNumber.group());
        if (wrapper != null && wrapper.start() == -1) {
            // Special case: start == -1 means the link is invalid (e.g., wider link)
            return null;
        }

        if (wrapper == null) {
            wrapper = findNonBreakingSpaceWrapper(text, start, end);
        }

        if (wrapper == null) {
            wrapper = findEraTemplateWrapper(text, start, end);
        }

        // Build the final CenturyMatch with wrapper positions (or original if no wrapper)
        final int finalStart = wrapper != null ? wrapper.start() : start;
        final String finalGroup = wrapper != null ? wrapper.group() : text.substring(start, end);
        return new CenturyMatch(finalStart, finalGroup, centuryWord, centuryNumber);
    }

    @Nullable
    private MatchResult findLinkWrapper(String text, int start, int end, String centuryNumber) {
        final MatchResult endLink = FinderUtils.indexOfAny(text, end, START_LINK, END_LINK);
        final boolean endsWithLink = endLink != null && END_LINK.equals(endLink.group());
        if (!endsWithLink) {
            return null;
        }

        // Check if the century is followed immediately by the link end
        // Exception: an aliased link, e.g. "[[Siglo XX|XX]]"
        boolean isAliased =
            text.charAt(end) == PIPE &&
            end + 1 + centuryNumber.length() == endLink.start() &&
            ReplacerUtils.containsAtPosition(text, centuryNumber, end + 1);
        if (!isAliased && !FinderUtils.isEmptyBlankOrSpaceAlias(text, end, endLink.start())) {
            // Invalid: there's content between century and link end that's not an alias
            // This means it's a wider link like "[[Siglo XX en España]]"
            // Return a special marker to indicate this wrapper should be rejected entirely
            return FinderMatchRange.of(text, -1, -1);
        }

        // Check if the century is preceded immediately by the link start
        final MatchResult startLink = FinderUtils.lastIndexOfAny(text, start, START_LINK, END_LINK);
        final boolean startsWithLink = startLink != null && START_LINK.equals(startLink.group());
        if (!startsWithLink) {
            return null;
        }

        if (!FinderUtils.isEmptyBlankOrSpaceAlias(text, startLink.end(), start)) {
            // Invalid: there's content between link start and century word
            // This means it's a wider link like "[[Terremotos en el siglo XX]]"
            // Return a special marker to indicate this wrapper should be rejected entirely
            return FinderMatchRange.of(text, -1, -1);
        }

        // At this point the link is valid
        return FinderMatchRange.of(text, startLink.start(), endLink.end());
    }

    @Nullable
    private MatchResult findNonBreakingSpaceWrapper(String text, int start, int end) {
        for (String wrapTemplate : WRAP_TEMPLATES) {
            final String nbsTemplatePrefix = START_TEMPLATE + wrapTemplate + PIPE;
            final int startNbsTemplate = start - nbsTemplatePrefix.length();
            final boolean startsWithNbsTemplate = ReplacerUtils.containsAtPosition(
                text,
                nbsTemplatePrefix,
                startNbsTemplate
            );
            if (startsWithNbsTemplate && ReplacerUtils.containsAtPosition(text, END_TEMPLATE, end)) {
                return FinderMatchRange.of(text, startNbsTemplate, end + END_TEMPLATE.length());
            }
        }
        return null;
    }

    @Nullable
    private MatchResult findEraTemplateWrapper(String text, int start, int end) {
        final String eraTemplatePrefix = START_TEMPLATE + "AC" + PIPE;
        final int startEraTemplate = start - eraTemplatePrefix.length();
        final boolean startsWithEraTemplate =
            startEraTemplate >= 0 && eraTemplatePrefix.equalsIgnoreCase(text.substring(startEraTemplate, start));
        if (startsWithEraTemplate && ReplacerUtils.containsAtPosition(text, END_TEMPLATE, end)) {
            return FinderMatchRange.of(text, startEraTemplate, end + END_TEMPLATE.length());
        }
        return null;
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
        int numWordsFound = 0;
        int wordStart = start;
        while (numWordsFound <= MAX_WORDS_BETWEEN_CENTURIES) {
            final MatchResult nextWord = FinderUtils.findWordAfter(text, wordStart);
            // Stop if no more words or if there's a line break (centuries must be on the same line)
            if (nextWord == null || text.indexOf('\n', wordStart, nextWord.start()) >= 0) {
                return null;
            }

            // Check if it is a century number and is greater than the previous one
            // We "find" instead of "get" to capture a possible era in the extension
            final CenturyNumber nextNumber = findCenturyNumber(text, nextWord.start());
            if (nextNumber != null && nextNumber.isGreaterThan(currentNumber)) {
                if (containsCompleteCentury(text, start, nextNumber.start())) {
                    return null;
                }
                return CenturyMatch.ofCenturyNumber(nextNumber);
            }

            numWordsFound++;
            wordStart = nextWord.end();
        }
        return null;
    }

    /** Check if there's a complete century word between the given positions */
    private boolean containsCompleteCentury(String text, int start, int end) {
        for (String centuryWord : CENTURY_WORDS) {
            if (text.indexOf(centuryWord, start, end) >= 0) {
                return true;
            }
        }
        return false;
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

    private boolean isAbbreviation(String word) {
        return word.length() == 2;
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

    //region Automatic replacements

    @Override
    public Stream<Replacement> findAutomaticReplacements(FinderPage page) {
        final SequencedCollection<MatchResult> automaticMatches = new ArrayList<>();
        int start = 0;
        while (start >= 0 && start < page.getContent().length()) {
            final SequencedCollection<MatchResult> matches = this.findCenturyMatches(page, start);
            if (matches.isEmpty()) {
                // End the loop
                start = -1;
            } else {
                if (isAutomaticReplacement(matches, page)) {
                    automaticMatches.addAll(matches);
                }
                start = matches.getLast().end();
            }
        }
        // Filter just in case the ones with more than one possible fix
        return automaticMatches.stream().map(m -> this.convert(m, page)).filter(r -> r.suggestions().size() == 2);
    }

    // We need to capture all the century subgroups
    private boolean isAutomaticReplacement(SequencedCollection<MatchResult> matches, FinderPage page) {
        assert !matches.isEmpty();

        final MatchResult firstMatchResult = matches.getFirst();
        assert firstMatchResult instanceof CenturyMatch;
        final CenturyMatch firstMatch = (CenturyMatch) firstMatchResult;
        final MatchResult firstWord = firstMatch.word();

        if (matches.size() == 1) {
            // Case 1: only one century
            // Only automatic if the first word is complete and lowercase, e.g. "siglo XX",
            // and the number is Roman uppercase
            final String number = firstMatch.number().getOriginalNumber();
            return (
                firstWord != null &&
                firstWord.start() == 0 &&
                "siglo".equals(firstWord.group()) &&
                isUpperCaseRoman(number)
            );
        }

        assert matches.size() > 1;

        // The numbers must all be uppercase Roman
        boolean allUpperCaseRoman = matches
            .stream()
            .map(m -> ((CenturyMatch) m).number().roman())
            .allMatch(this::isUpperCaseRoman);
        if (!allUpperCaseRoman) {
            return false;
        }

        // If the first match has a word it must be lowercase and not an abbreviation
        if (firstWord != null) {
            final String word = firstWord.group();
            return !isAbbreviation(word) && FinderUtils.startsWithLowerCase(word);
        }

        return true;
    }

    private boolean isUpperCaseRoman(String roman) {
        return StringUtils.isAllUpperCase(roman) && ROMAN_TO_ARABIC.containsKey(roman);
    }
    //endregion
}
