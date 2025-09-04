package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.END_LINK;
import static es.bvalero.replacer.finder.util.FinderUtils.START_LINK;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.roman.code.ConvertToArabic;
import com.roman.code.exception.ConversionException;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
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
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find a century and replace it with the template
 */
@Component
class CenturyFinder implements ReplacementFinder {

    private static final String CENTURY_WORD = "siglo";
    private static final String CENTURY_SEARCH = CENTURY_WORD.substring(1);
    private static final char PLURAL_LETTER = 's';
    private static final char SPACE = ' ';
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

            int startCentury = centuryWord.start();
            int endCentury = centuryWord.end();

            // Find the century number
            assert text.charAt(endCentury) == SPACE;
            final MatchResult centuryNumber = findCenturyNumber(text, endCentury + 1);
            if (centuryNumber == null) {
                start = endCentury + 1;
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

            // Check if the century is surrounded by a link
            final Boolean isLinked = isLinked(text, startCentury, endCentury);
            if (isLinked == null) {
                start = endCentury + 1;
                continue;
            } else if (isLinked) {
                startCentury -= START_LINK.length();
                endCentury += END_LINK.length();
            }

            // Find the extension (optional)
            MatchResult extension = findExtension(text, endCentury, centuryNumber.group());
            if (extension == null) {
                // Fake empty match result
                extension = FinderMatchResult.ofEmpty(endCentury);
            }

            // If the century word is plural then the extension is mandatory
            if (isPlural(centuryWord.group()) && StringUtils.isEmpty(extension.group())) {
                start = endCentury + 1;
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
        // Instead of making a case-insensitive search, we find the end of the word,
        // and then we check the word is complete. It is a little faster than finding the word with both cases.
        while (start >= 0 && start < text.length()) {
            final int startSuffix = text.indexOf(CENTURY_SEARCH, start);
            int endCentury = startSuffix + CENTURY_SEARCH.length();
            if (startSuffix <= 0 || endCentury >= text.length()) { // <= 0 to be able to get the character before
                return null;
            }

            // Check first letter
            final int startCentury = startSuffix - 1;
            final char firstLetter = text.charAt(startCentury);
            if (firstLetter != 'S' && firstLetter != 's') {
                start = endCentury;
                continue;
            }

            // Century word can be plural
            final char pluralLetter = text.charAt(endCentury);
            if (pluralLetter == PLURAL_LETTER) {
                endCentury++;
            }

            // We only consider the word complete and followed by a whitespace
            if (endCentury >= text.length()) {
                return null;
            }
            final String centuryWord = text.substring(startCentury, endCentury);
            final char spaceLetter = text.charAt(endCentury);
            if (spaceLetter != SPACE || !FinderUtils.isWordCompleteInText(startCentury, centuryWord, text)) {
                start = endCentury;
                continue;
            }

            return FinderMatchResult.of(startCentury, centuryWord);
        }
        return null;
    }

    @Nullable
    private MatchResult findCenturyNumber(String text, int start) {
        final MatchResult centuryNumber = FinderUtils.findWordAfter(text, start);
        if (centuryNumber == null || centuryNumber.start() != start || !isCenturyNumber(centuryNumber.group())) {
            return null;
        }
        return centuryNumber;
    }

    private boolean isCenturyNumber(String text) {
        final String upperText = FinderUtils.toUpperCase(text);

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
        // 11 is the max length of an era including a space before
        final String nextText = text.substring(start, Math.min(start + 11, text.length()));
        return ERA_WORDS.stream()
            .filter(w -> containsEra(nextText, w))
            .findFirst()
            .map(w -> FinderMatchResult.of(start, w))
            .orElse(null);
    }

    private boolean containsEra(String text, String eraWord) {
        final int pos = text.indexOf(eraWord);
        if (pos >= 0) {
            return FinderUtils.isActualSpace(text.substring(0, pos));
        }
        return false;
    }

    /** True if linked; False if not linked; Null if linked not closed or open. */
    @Nullable
    private Boolean isLinked(String text, int start, int end) {
        final int startLink = Math.max(0, start - START_LINK.length());
        final int endLink = Math.min(text.length(), end + END_LINK.length());
        final String leftLink = text.substring(startLink, start);
        final String rightLink = text.substring(end, endLink);
        if (START_LINK.equals(leftLink) && END_LINK.equals(rightLink)) {
            return true;
        } else if (START_LINK.equals(leftLink) || END_LINK.equals(rightLink)) {
            return null;
        } else {
            return false;
        }
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

        String centuryText = match.group();
        final String centuryWord = match.group(1);
        final boolean isUppercase = FinderUtils.startsWithUpperCase(centuryWord);
        final String centuryNumber = FinderUtils.toUpperCase(match.group(2));
        final String eraText = match.group(3);
        final String eraLetter = StringUtils.isEmpty(eraText) ? EMPTY : String.valueOf(eraText.charAt(0));
        final boolean isLinked = centuryText.startsWith(START_LINK);

        // Add extension and its suggestion
        final String extensionText = match.group(4);
        final String extension;
        if (StringUtils.isEmpty(extensionText)) {
            extension = EMPTY;
        } else {
            final int extensionStart = match.start(4);
            final int extensionEnd = match.end(4);
            final String extensionPrefix = text.substring(match.end(), extensionStart);
            centuryText = text.substring(match.start(), extensionEnd);
            extension = extensionPrefix + fixSimpleCentury(extensionText);
        }

        // Templates
        final String templateUpperLink =
            "{{" + centuryWord + "|" + centuryNumber + "|" + eraLetter + "|S|1}}" + extension;
        final String templateUpperNoLink =
            "{{" + centuryWord + "|" + centuryNumber + "|" + eraLetter + "|S}}" + extension;
        final String templateLowerLink =
            "{{" + centuryWord + "|" + centuryNumber + "|" + eraLetter + "|s|1}}" + extension;
        final String templateLowerNoLink =
            "{{" + centuryWord + "|" + centuryNumber + "|" + eraLetter + "|s}}" + extension;
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

        return Replacement.of(match.start(), centuryText, StandardType.CENTURY, suggestions, text);
    }

    private Replacement convertCenturyPlural(MatchResult match, FinderPage page) {
        final String text = page.getContent();

        final String centuryText = text.substring(match.start(), match.end(4));

        final List<Suggestion> suggestions = new ArrayList<>(1);
        final String suggestionText =
            text.substring(match.start(0), match.start(2)) +
            fixSimpleCentury(match.group(2)) +
            text.substring(match.end(2), match.start(4)) +
            fixSimpleCentury(match.group(4));

        suggestions.add(Suggestion.of(suggestionText, "siglos en versalitas"));

        return Replacement.of(match.start(), centuryText, StandardType.CENTURY, suggestions, text);
    }

    private String fixSimpleCentury(String century) {
        return "{{Siglo|" + century + "}}";
    }
}
