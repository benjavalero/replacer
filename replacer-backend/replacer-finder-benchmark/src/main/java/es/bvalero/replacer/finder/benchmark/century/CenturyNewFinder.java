package es.bvalero.replacer.finder.benchmark.century;

import static es.bvalero.replacer.finder.util.FinderUtils.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.roman.code.ConvertToArabic;
import com.roman.code.exception.ConversionException;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

class CenturyNewFinder implements ReplacementFinder {

    private static final String CENTURY_WORD = "Siglo";
    private static final String CENTURY_SEARCH = CENTURY_WORD.substring(1);
    private static final char PLURAL_LETTER = 's';
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
                era = FinderMatchResult.ofEmpty(endCentury);
            }
            endCentury = era.end();

            // Find if the century is wrapped so the positions must be modified
            final MatchResult wrap = findCenturyWrapper(text, startCentury, endCentury, centuryNumber.group());
            if (wrap != null) {
                startCentury = wrap.start();
                endCentury = wrap.end();
            }

            // Find the extension (optional)
            MatchResult extension = findExtension(text, endCentury, centuryNumber.group());
            if (extension == null) {
                // Fake empty match result
                extension = FinderMatchResult.ofEmpty(endCentury);
            }
            endCentury = extension.end();

            // If the century word is plural then the extension is mandatory
            if (isPlural(centuryWord.group()) && StringUtils.isEmpty(extension.group())) {
                start = endCentury;
                continue;
            }

            final FinderMatchResult match = FinderMatchResult.ofNested(text, startCentury, endCentury);
            match.addGroup(centuryWord);
            match.addGroup(centuryNumber);
            match.addGroup(era);
            match.addGroup(extension);
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
                return isAbbreviation ? match : FinderMatchResult.of(text, startCentury, endCentury);
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
        // The century library only accepts uppercase characters
        final String romanNumber = ReplacerUtils.toUpperCase(centuryNumber.group());
        // Note that we are returning the match with uppercase, although maybe it is not in the whole text.
        return isCenturyNumber(romanNumber) ? FinderMatchResult.of(centuryNumber.start(), romanNumber) : null;
    }

    private boolean isCenturyNumber(String text) {
        // Check the century number only contains valid century letters
        for (int i = 0; i < text.length(); i++) {
            if (!isCenturyLetter(text.charAt(i))) {
                return false;
            }
        }

        //  Check the century number is valid and lower than the current century
        try {
            assert FinderUtils.isUppercase(text);
            return ConvertToArabic.fromRoman(text) <= 21;
        } catch (ConversionException ce) {
            return false;
        }
    }

    private boolean isCenturyLetter(char ch) {
        return ch == 'I' || ch == 'V' || ch == 'X';
    }

    @Nullable
    private MatchResult findEra(String text, int start) {
        final MatchResult nextWord = FinderUtils.findWordAfterSpace(text, start);
        if (nextWord == null) {
            return null;
        }
        final int startNextWord = nextWord.start();
        return ERA_WORDS.stream()
            .filter(w -> ReplacerUtils.containsAtPosition(text, w, startNextWord))
            .findAny()
            .map(w -> FinderMatchResult.of(startNextWord, w))
            .orElse(null);
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
                return FinderMatchResult.of(text, posStartLink, end + END_LINK.length());
            }
            // Check link with alias
            if (
                text.charAt(end) == PIPE &&
                end + 1 + centuryNumber.length() == posEndLink &&
                ReplacerUtils.containsAtPosition(text, centuryNumber, end + 1)
            ) {
                return FinderMatchResult.of(text, posStartLink, posEndLink + END_LINK.length());
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
            return FinderMatchResult.of(text, startNbsTemplate, end + END_TEMPLATE.length());
        }

        // Check if wrapped by an era template
        final String eraTemplatePrefix = START_TEMPLATE + "AC" + PIPE;
        final int startEraTemplate = start - eraTemplatePrefix.length();
        final boolean startsWithEraTemplate =
            startEraTemplate >= 0 && eraTemplatePrefix.equalsIgnoreCase(text.substring(startEraTemplate, start));
        if (startsWithEraTemplate && ReplacerUtils.containsAtPosition(text, END_TEMPLATE, end)) {
            return FinderMatchResult.of(text, startEraTemplate, end + END_TEMPLATE.length());
        }

        return null;
    }

    @Nullable
    private MatchResult findExtension(String text, int start, String centuryNumber) {
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
            // The century library only accepts uppercase characters
            final String romanWord = ReplacerUtils.toUpperCase(word);
            if (
                isCenturyNumber(romanWord) &&
                ConvertToArabic.fromRoman(romanWord) > ConvertToArabic.fromRoman(centuryNumber)
            ) {
                return wordFound;
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
        final String centuryNumber = match.group(2);
        assert FinderUtils.isUppercase(centuryNumber);
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
        final String extensionText = match.group(4);
        final String extension;
        if (StringUtils.isEmpty(extensionText)) {
            extension = EMPTY;
        } else {
            final int extensionStart = match.start(4);
            final int eraEnd = match.end(3);
            extension = text.substring(eraEnd, extensionStart) + fixSimpleCentury(extensionText);
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
        final int startExtension = match.start(4);
        final String era = text.substring(endNumber, startExtension); // Including spaces
        final String extensionText = match.group(4);
        final String fixedExtension = fixSimpleCentury(extensionText);
        final String suggestionText = centuryWord + fixedNumber + era + fixedExtension;

        final List<Suggestion> suggestions = List.of(Suggestion.of(suggestionText, "siglos en versalitas"));

        return Replacement.of(match.start(), match.group(), StandardType.CENTURY, suggestions);
    }

    private String fixSimpleCentury(String century) {
        return "{{Siglo|" + ReplacerUtils.toUpperCase(century) + "}}";
    }
}
