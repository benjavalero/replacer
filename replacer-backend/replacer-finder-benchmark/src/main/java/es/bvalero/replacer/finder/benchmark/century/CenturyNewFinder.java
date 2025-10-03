package es.bvalero.replacer.finder.benchmark.century;

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
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.SequencedCollection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import javax.naming.OperationNotSupportedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

class CenturyNewFinder implements ReplacementFinder {

    private static final String CENTURY_WORD = "Siglo";
    private static final String CENTURY_SEARCH = CENTURY_WORD.substring(1);

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        if (WikipediaLanguage.SPANISH == page.getPageKey().getLang()) {
            return LinearMatchFinder.find(page, this::findCentury);
        } else {
            return Stream.of();
        }
    }

    @Nullable
    private MatchResult findCentury(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            // 1. Find century word
            final MatchResult centuryWord = findCenturyWord(text, start);
            if (centuryWord == null) {
                return null;
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
            if (wrap != null) {
                startCentury = wrap.start();
                endCentury = wrap.end();
            }

            // 4. Find the extensions (optional)
            SequencedCollection<MatchResult> extensions = findExtensions(text, endCentury, centuryNumber.start(2));
            if (!extensions.isEmpty()) {
                endCentury = extensions.getLast().end();
            }

            // If the century word is plural then the extension is mandatory
            if (isPlural(centuryWord.group()) && extensions.isEmpty()) {
                start = endCentury;
                continue;
            }

            final FinderMatchRange match = FinderMatchRange.ofNested(text, startCentury, endCentury);
            match.addGroup(centuryWord);
            match.addGroup(centuryNumber);
            for (MatchResult extension : extensions) {
                match.addGroup(extension);
            }
            return match;
        }
        return null;
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
     * The century number is stored in the 1st nested match group.
     * Trick: the Arabic value is stored in the start of the 2nd nested match group.
     * The era, if existing, is stored in the 3rd nested match group.
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

    /** Find if the century (word and number) is wrapped by a link or a known template */
    @Nullable
    private MatchResult findCenturyWrapper(String text, int start, int end, String centuryNumber) {
        // Check if wrapped by a link
        final boolean startsWithLink = ReplacerUtils.containsAtPosition(text, START_LINK, start - START_LINK.length());
        if (startsWithLink) {
            final int posStartLink = start - START_LINK.length();
            final int posEndLink = text.indexOf(END_LINK, end);
            if (posEndLink < 0) {
                // Broken link
                return null;
            }
            if (posEndLink == end) {
                // Linked without alias
                return FinderMatchRange.of(text, posStartLink, end + END_LINK.length());
            }
            // Check link with alias
            if (
                text.charAt(end) == PIPE &&
                end + 1 + centuryNumber.length() == posEndLink &&
                ReplacerUtils.containsAtPosition(text, centuryNumber, end + 1)
            ) {
                return FinderMatchRange.of(text, posStartLink, posEndLink + END_LINK.length());
            }
        }

        // Check if wrapped by a non-breaking space template
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

        // Check if wrapped by an era template
        final String eraTemplatePrefix = START_TEMPLATE + "AC" + PIPE;
        final int startEraTemplate = start - eraTemplatePrefix.length();
        final boolean startsWithEraTemplate =
            startEraTemplate >= 0 && eraTemplatePrefix.equalsIgnoreCase(text.substring(startEraTemplate, start));
        if (startsWithEraTemplate && ReplacerUtils.containsAtPosition(text, END_TEMPLATE, end)) {
            return FinderMatchRange.of(text, startEraTemplate, end + END_TEMPLATE.length());
        }

        return null;
    }

    /** Find the existing extensions to the century, i.e. more century numbers close to the found century. */
    private SequencedCollection<MatchResult> findExtensions(String text, int start, int centuryNumber) {
        final SequencedCollection<MatchResult> extensions = new ArrayList<>(2);
        MatchResult extension = findExtension(text, start, centuryNumber);
        while (extension != null) {
            extensions.add(extension);
            extension = findExtension(text, extension.end(), extension.start(1));
        }
        return extensions;
    }

    /** Find a valid century number close enough */
    @Nullable
    private MatchResult findExtension(String text, int start, int centuryNumber) {
        // Find the next century number with at most 3 words between both century numbers
        int numWordsFound = 0;
        int wordStart = start;
        while (numWordsFound < 4) {
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
            if (numberMatch != null && numberMatch.start(2) > centuryNumber) {
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
        if (isPlural(centuryWord)) {
            return convertCenturyPlural(match, page);
        } else {
            return convertCenturySingular(match, page);
        }
    }

    private Replacement convertCenturySingular(MatchResult matchResult, FinderPage page) {
        final String text = page.getContent();
        final FinderMatchRange match = (FinderMatchRange) matchResult;

        final String centuryWord = match.group(1);
        final boolean isUppercase = FinderUtils.startsWithUpperCase(centuryWord);
        final MatchResult centuryNumberMatch = match.getGroup(2);
        final String centuryNumber = centuryNumberMatch.group(2);
        String eraLetter;
        if (centuryNumberMatch.groupCount() > 2) {
            final String eraText = centuryNumberMatch.group(3);
            eraLetter = String.valueOf(eraText.charAt(0));
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

        // Add extension and its suggestion
        StringBuilder extension = new StringBuilder();
        if (match.groupCount() > 2) {
            for (int i = 3; i <= match.groupCount(); i++) {
                final int extensionStart = match.start(i);
                final int previousEnd = match.end(i - 1);
                final MatchResult extensionMatch = match.getGroup(i);
                extension.append(text, previousEnd, extensionStart).append(fixCenturyNumber(extensionMatch));
            }
        }

        // Templates (as simple as possible)
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
        final String templateUpperLink =
            "{{" + templateName + "|" + centuryNumber + "|" + eraLetter + "|" + upperPrefix + "|1}}" + extension;
        final String templateUpperNoLink =
            ("{{" +
                templateName +
                "|" +
                centuryNumber +
                "|" +
                eraLetter +
                "|" +
                upperPrefix +
                "}}" +
                extension).replace("||}}", "}}");
        final String templateLowerLink =
            "{{" + templateName + "|" + centuryNumber + "|" + eraLetter + "|" + lowerPrefix + "|1}}" + extension;
        final String templateLowerNoLink =
            ("{{" +
                templateName +
                "|" +
                centuryNumber +
                "|" +
                eraLetter +
                "|" +
                lowerPrefix +
                "}}" +
                extension).replace("||}}", "}}");
        final String linkedComment = "enlazado —solo para temas relacionados con el calendario—";

        final List<Suggestion> suggestions = new ArrayList<>(4);

        // Not linked centuries are recommended
        // Offer always the lowercase alternative
        if (isUppercase) {
            suggestions.add(Suggestion.of(templateUpperNoLink, "siglo en versalitas; con mayúscula; sin enlazar"));
            if (isLinked) {
                suggestions.add(
                    Suggestion.of(templateUpperLink, "siglo en versalitas; con mayúscula; " + linkedComment)
                );
            }
        }
        suggestions.add(Suggestion.of(templateLowerNoLink, "siglo en versalitas; con minúscula; sin enlazar"));
        if (isLinked) {
            suggestions.add(Suggestion.of(templateLowerLink, "siglo en versalitas; con minúscula; " + linkedComment));
        }

        return Replacement.of(match.start(), match.group(), StandardType.CENTURY, suggestions);
    }

    private Replacement convertCenturyPlural(MatchResult matchResult, FinderPage page) {
        final String text = page.getContent();
        final FinderMatchRange match = (FinderMatchRange) matchResult;

        assert match.start(1) == match.start();
        final int startWord = match.start();
        final int startNumber = match.start(2);
        final String centuryWord = text.substring(startWord, startNumber); // Including space between word and number

        final MatchResult centuryNumberMatch = match.getGroup(2);
        final String fixedNumber = fixCenturyNumber(centuryNumberMatch);

        StringBuilder extension = new StringBuilder();
        assert match.groupCount() > 2;
        for (int i = 3; i <= match.groupCount(); i++) {
            final int extensionStart = match.start(i);
            final int previousEnd = match.end(i - 1);
            final MatchResult extensionMatch = match.getGroup(i);
            extension.append(text, previousEnd, extensionStart).append(fixCenturyNumber(extensionMatch));
        }
        final String suggestionText = centuryWord + fixedNumber + extension;

        final List<Suggestion> suggestions = List.of(Suggestion.of(suggestionText, "siglos en versalitas"));

        return Replacement.of(match.start(), match.group(), StandardType.CENTURY, suggestions);
    }

    private String fixCenturyNumber(MatchResult match) {
        // We now that it is a century number match
        final StringBuilder fix = new StringBuilder()
            .append(START_TEMPLATE)
            .append(CENTURY_WORD)
            .append(PIPE)
            .append(match.group(2));
        if (match.groupCount() > 2) {
            fix.append(PIPE).append(match.group(3).charAt(0));
        }
        fix.append(END_TEMPLATE);
        return fix.toString();
    }
}
