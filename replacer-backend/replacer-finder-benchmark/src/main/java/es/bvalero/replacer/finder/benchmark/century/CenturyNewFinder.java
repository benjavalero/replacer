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
    private static final char PLURAL_LETTER = 's';

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
            // Find century word. It may start with uppercase or be plural, or be an abbreviation.
            final MatchResult centuryWord = findCenturyWord(text, start);
            if (centuryWord == null) {
                return null;
            }

            int startCentury = centuryWord.start();
            int endCentury = centuryWord.end();

            // Find the century number
            final MatchResult centuryNumber = findCenturyNumber(text, endCentury);
            if (centuryNumber == null) {
                start = endCentury;
                continue;
            } else {
                endCentury = centuryNumber.end();
            }

            // Find the era (optional)
            MatchResult era = findEra(text, endCentury);
            if (era == null) {
                // Fake empty match result
                era = FinderMatchRange.ofEmpty(text, endCentury);
            }
            endCentury = era.end();

            // Find if the century is wrapped so the positions must be modified
            final MatchResult wrap = findCenturyWrapper(text, startCentury, endCentury, centuryNumber.group());
            if (wrap != null) {
                startCentury = wrap.start();
                endCentury = wrap.end();
            }

            // Find the extensions (optional)
            SequencedCollection<MatchResult> extensions = findExtensions(text, endCentury, centuryNumber.start(1));
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
            match.addGroup(era);
            for (MatchResult extension : extensions) {
                match.addGroup(extension);
            }
            return match;
        }
        return null;
    }

    @Nullable
    private MatchResult findCenturyWord(String text, int start) {
        while (start >= 0 && start < text.length()) {
            final MatchResult match = FinderUtils.indexOfAny(text, start, "s.", "S.", CENTURY_SEARCH);
            if (match == null) {
                return null;
            }

            int startCentury = match.start();
            int endCentury = match.end();
            final boolean isAbbreviation = isAbbreviated(match.group());
            if (!isAbbreviation) {
                // Check first letter
                if (startCentury <= 0) {
                    start = endCentury;
                    continue;
                }
                final char firstLetter = text.charAt(--startCentury);
                if (firstLetter != 'S' && firstLetter != 's') {
                    start = endCentury;
                    continue;
                }

                // Century word can be plural
                if (text.charAt(endCentury) == PLURAL_LETTER) {
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

    private boolean isAbbreviated(String word) {
        return word.charAt(word.length() - 1) == '.';
    }

    @Nullable
    private MatchResult findCenturyNumber(String text, int start) {
        final MatchResult centuryNumber = FinderUtils.findWordAfterSpace(text, start);
        if (centuryNumber == null) {
            return null;
        }
        final int arabicNumber = getCenturyArabicNumber(centuryNumber.group());
        if (arabicNumber > 0) {
            // Trick: store the Arabic value in the start a nested match group
            final FinderMatchResult match = FinderMatchResult.ofNested(centuryNumber.start(), centuryNumber.group());
            match.addGroup(FinderMatchRange.ofEmpty(text, arabicNumber));
            return match;
        }
        return null;
    }

    private int getCenturyArabicNumber(String word) {
        // Note the word can be a Roman number in lowercase or an Arabic number
        try {
            int arabic = -1;
            if (isRomanLetters(word)) {
                final String upperRoman = ReplacerUtils.toUpperCase(word);
                arabic = ConvertToArabic.fromRoman(upperRoman);
            } else if (word.length() <= 2 && FinderUtils.isNumber(word)) {
                arabic = Integer.parseInt(word);
            }
            if (isValidArabicCentury(arabic)) {
                return arabic;
            }
        } catch (ConversionException ignored) {}
        return -1;
    }

    @Nullable
    private String getCenturyRomanNumber(String word) {
        // Note the word can be a Roman number in lowercase or an Arabic number
        try {
            if (isRomanLetters(word)) {
                final String upperRoman = ReplacerUtils.toUpperCase(word);
                if (isValidArabicCentury(ConvertToArabic.fromRoman(upperRoman))) {
                    return upperRoman;
                }
            } else if (word.length() <= 2 && FinderUtils.isNumber(word)) {
                final int arabic = Integer.parseInt(word);
                if (isValidArabicCentury(arabic)) {
                    return ConvertToRoman.fromArabic(arabic);
                }
            }
        } catch (ConversionException | OperationNotSupportedException ignored) {}
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

    @Nullable
    private MatchResult findEra(String text, int start) {
        final MatchResult firstWord = FinderUtils.findWordAfterSpace(text, start);
        if (firstWord == null) {
            return null;
        }
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
        // Find the second part of the era
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

    private SequencedCollection<MatchResult> findExtensions(String text, int start, int centuryNumber) {
        final SequencedCollection<MatchResult> extensions = new ArrayList<>(2);
        MatchResult extension = findExtension(text, start, centuryNumber);
        while (extension != null) {
            extensions.add(extension);
            extension = findExtension(text, extension.end(), extension.start(1));
        }
        return extensions;
    }

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
            final String word = wordFound.group();
            if (word.contains(CENTURY_SEARCH)) {
                return null;
            }

            // Check the found century number is actually a century
            // Check the next century number is greater than the previous one
            final int extensionNumber = getCenturyArabicNumber(word);
            if (extensionNumber > centuryNumber) {
                // Trick: store the Arabic value in the start a nested match group
                final FinderMatchResult match = FinderMatchResult.ofNested(wordFound.start(), wordFound.group());
                match.addGroup(FinderMatchRange.ofEmpty(text, extensionNumber));
                return match;
            } else {
                numWordsFound++;
                wordStart = wordFound.end();
            }
        }
        return null;
    }

    private boolean isPlural(String word) {
        return word.charAt(word.length() - 1) == PLURAL_LETTER;
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

    private Replacement convertCenturySingular(MatchResult match, FinderPage page) {
        final String text = page.getContent();

        final String centuryWord = match.group(1);
        final boolean isUppercase = FinderUtils.startsWithUpperCase(centuryWord);
        final String centuryNumber = getCenturyRomanNumber(match.group(2));
        final String eraText = match.group(3);
        String eraLetter;
        if (StringUtils.isNotEmpty(eraText)) {
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
        if (match.groupCount() > 3) {
            for (int i = 4; i <= match.groupCount(); i++) {
                final int extensionStart = match.start(i);
                final int previousEnd = match.end(i - 1);
                final String extensionText = match.group(i);
                extension.append(text, previousEnd, extensionStart).append(fixSimpleCentury(extensionText));
            }
        }

        // Templates (as simple as possible)
        final boolean isAbbreviated = isAbbreviated(centuryWord);
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

    private Replacement convertCenturyPlural(MatchResult match, FinderPage page) {
        final String text = page.getContent();

        final int startWord = match.start(0);
        final int startNumber = match.start(2);
        final String centuryWord = text.substring(startWord, startNumber); // Including space after
        final String centuryNumber = match.group(2);
        final String fixedNumber = fixSimpleCentury(centuryNumber);
        final int endNumber = match.end(2);
        final String era = text.substring(endNumber, match.end(3)); // Including spaces
        StringBuilder extension = new StringBuilder();
        assert match.groupCount() > 3;
        for (int i = 4; i <= match.groupCount(); i++) {
            final int extensionStart = match.start(i);
            final int previousEnd = match.end(i - 1);
            final String extensionText = match.group(i);
            extension.append(text, previousEnd, extensionStart).append(fixSimpleCentury(extensionText));
        }
        final String suggestionText = centuryWord + fixedNumber + era + extension;

        final List<Suggestion> suggestions = List.of(Suggestion.of(suggestionText, "siglos en versalitas"));

        return Replacement.of(match.start(), match.group(), StandardType.CENTURY, suggestions);
    }

    private String fixSimpleCentury(String century) {
        return START_TEMPLATE + CENTURY_WORD + PIPE + getCenturyRomanNumber(century) + END_TEMPLATE;
    }
}
