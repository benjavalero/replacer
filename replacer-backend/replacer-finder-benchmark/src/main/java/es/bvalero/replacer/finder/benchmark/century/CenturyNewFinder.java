package es.bvalero.replacer.finder.benchmark.century;

import static es.bvalero.replacer.finder.util.FinderUtils.END_LINK;
import static es.bvalero.replacer.finder.util.FinderUtils.START_LINK;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.roman.code.ConvertToArabic;
import com.roman.code.exception.ConversionException;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

class CenturyNewFinder implements BenchmarkFinder {

    private static final String CENTURY_WORD = "Siglo";
    private static final String CENTURY_SEARCH = CENTURY_WORD.substring(1);
    private static final char PLURAL_LETTER = 's';
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
            // Find century word. It may start with uppercase or be plural.
            final MatchResult centuryWord = findCenturyWord(text, start);
            if (centuryWord == null) {
                return null;
            }

            final int startCentury = centuryWord.start();
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

            // Find the extension (optional)
            MatchResult extension = findExtension(text, endCentury, centuryNumber.group());
            if (extension == null) {
                // Fake empty match result
                extension = FinderMatchResult.ofEmpty(endCentury);
            }

            // If the century word is plural then the extension is mandatory
            if (isPlural(centuryWord.group()) && StringUtils.isEmpty(extension.group())) {
                start = endCentury;
                continue;
            }

            final FinderMatchResult match = FinderMatchResult.of(text, startCentury, endCentury);
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
            final boolean isAbbreviation = !CENTURY_SEARCH.equals(match.group());
            if (!isAbbreviation) {
                // Check first letter
                startCentury--;
                if (match.start() < 0) {
                    start = endCentury;
                    continue;
                }
                final char firstLetter = text.charAt(startCentury);
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

    @Nullable
    private MatchResult findCenturyNumber(String text, int start) {
        final MatchResult centuryNumber = FinderUtils.findWordAfterSpace(text, start);
        return centuryNumber != null && isCenturyNumber(centuryNumber.group()) ? centuryNumber : null;
    }

    private boolean isCenturyNumber(String text) {
        // The century library only accepts uppercase characters
        final String upperText = ReplacerUtils.toUpperCase(text);

        // Check the century number only contains valid century letters
        for (int i = 0; i < upperText.length(); i++) {
            if (!CENTURY_LETTERS.contains(upperText.charAt(i))) {
                return false;
            }
        }

        //  Check the century number is valid and lower than the current century
        try {
            return ConvertToArabic.fromRoman(upperText) <= 21;
        } catch (ConversionException ce) {
            return false;
        }
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

    private boolean isLinked(String text, int start, int end) {
        return (
            ReplacerUtils.containsAtPosition(text, START_LINK, Math.max(0, start - START_LINK.length())) &&
            ReplacerUtils.containsAtPosition(text, END_LINK, end)
        );
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
            if (isCenturyNumber(word) && ConvertToArabic.fromRoman(word) > ConvertToArabic.fromRoman(centuryNumber)) {
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
    public BenchmarkResult convert(MatchResult match, FinderPage page) {
        final String centuryWord = match.group(1);
        if (isPlural(centuryWord)) {
            return convertReplacement(convertCenturyPlural(match, page));
        } else {
            return convertReplacement(convertCenturySingular(match, page));
        }
    }

    private BenchmarkResult convertReplacement(Replacement replacement) {
        return BenchmarkResult.of(replacement.getStart(), replacement.getText());
    }

    private Replacement convertCenturySingular(MatchResult match, FinderPage page) {
        final String text = page.getContent();

        // String centuryText;
        int startCentury = match.start();
        int endCentury = match.end(3);
        String centuryWord = match.group(1);
        final boolean isUppercase = FinderUtils.startsWithUpperCase(centuryWord);
        final String centuryNumber = ReplacerUtils.toUpperCase(match.group(2));
        final String eraText = match.group(3);
        final String eraLetter = StringUtils.isEmpty(eraText) ? EMPTY : String.valueOf(eraText.charAt(0));

        // Check if the century is surrounded by a link
        final boolean isLinked = isLinked(text, startCentury, endCentury);
        if (isLinked) {
            startCentury -= START_LINK.length();
            endCentury += END_LINK.length();
        }

        // Add extension and its suggestion
        final String extensionText = match.group(4);
        final String extension;
        if (StringUtils.isEmpty(extensionText)) {
            extension = EMPTY;
        } else {
            final int extensionStart = match.start(4);
            endCentury = match.end(4);
            extension = text.substring(match.end(), extensionStart) + fixSimpleCentury(extensionText);
        }

        // Manage abbreviated century word
        final boolean isAbbreviated = centuryWord.charAt(centuryWord.length() - 1) == '.';
        if (isAbbreviated) {
            centuryWord = CENTURY_WORD;
        }

        // Templates
        final String lowerPrefix = isAbbreviated ? "a" : "s";
        final String upperPrefix = ReplacerUtils.toUpperCase(lowerPrefix);
        final String templateUpperLink =
            "{{" + centuryWord + "|" + centuryNumber + "|" + eraLetter + "|" + upperPrefix + "|1}}" + extension;
        final String templateUpperNoLink =
            "{{" + centuryWord + "|" + centuryNumber + "|" + eraLetter + "|" + upperPrefix + "}}" + extension;
        final String templateLowerLink =
            "{{" + centuryWord + "|" + centuryNumber + "|" + eraLetter + "|" + lowerPrefix + "|1}}" + extension;
        final String templateLowerNoLink =
            "{{" + centuryWord + "|" + centuryNumber + "|" + eraLetter + "|" + lowerPrefix + "}}" + extension;
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

        final String centuryText = text.substring(startCentury, endCentury);
        return Replacement.of(startCentury, centuryText, StandardType.CENTURY, suggestions, text);
    }

    private Replacement convertCenturyPlural(MatchResult match, FinderPage page) {
        final String text = page.getContent();

        final int startCentury = match.start();
        final int endExtension = match.end(4);
        final String centuryText = text.substring(startCentury, endExtension);

        final int startWord = match.start(0);
        final int startNumber = match.start(2);
        final String centuryWord = text.substring(startWord, startNumber); // Including space after
        final String centuryNumber = match.group(2);
        final String fixedNumber = fixSimpleCentury(centuryNumber);
        final int endNumber = match.end(2);
        final int startExtension = match.start(4);
        final String era = text.substring(endNumber, startExtension); // Including spaces
        final String extension = match.group(4);
        final String fixedExtension = fixSimpleCentury(extension);
        final String suggestionText = centuryWord + fixedNumber + era + fixedExtension;

        final List<Suggestion> suggestions = List.of(Suggestion.of(suggestionText, "siglos en versalitas"));

        return Replacement.of(match.start(), centuryText, StandardType.CENTURY, suggestions, text);
    }

    private String fixSimpleCentury(String century) {
        return "{{Siglo|" + century + "}}";
    }
}
