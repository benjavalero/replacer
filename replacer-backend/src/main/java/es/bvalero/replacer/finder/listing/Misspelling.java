package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Value;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.TestOnly;

/**
 * Domain class corresponding to a potential simple or composed misspelling.
 */
@Value
public class Misspelling {

    // Public as it is used also for some immutables

    @RegExp
    private static final String REGEX_BRACKETS = "\\([^)]+\\)";

    private static final Pattern PATTERN_BRACKETS = Pattern.compile(REGEX_BRACKETS);

    @RegExp
    private static final String REGEX_SUGGESTION = String.format("([^,(]|%s)+", REGEX_BRACKETS);

    private static final Pattern PATTERN_SUGGESTION = Pattern.compile(REGEX_SUGGESTION);

    String type;
    String word;
    boolean caseSensitive;
    List<Suggestion> suggestions;

    private Misspelling(String type, String word, boolean caseSensitive, String comment) {
        this.type = type;
        this.word = word;
        this.caseSensitive = caseSensitive;

        // Validate the word
        validateMisspellingWord(type, word);

        // Validate the comment and extract the suggestions
        this.suggestions = new ArrayList<>();
        try {
            this.suggestions.addAll(parseSuggestionsFromComment(comment));
            if (this.suggestions.isEmpty()) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Not valid misspelling comment: " + comment, e);
        }
    }

    public static Misspelling of(String type, String word, boolean caseSensitive, String comment) {
        return new Misspelling(type, word, caseSensitive, comment);
    }

    @TestOnly
    public static Misspelling of(String word, boolean caseSensitive, String comment) {
        return new Misspelling(ReplacementType.MISSPELLING_SIMPLE, word, caseSensitive, comment);
    }

    @TestOnly
    public static Misspelling ofCaseInsensitive(String word, String comment) {
        return Misspelling.of(ReplacementType.MISSPELLING_SIMPLE, word, false, comment);
    }

    @TestOnly
    public static Misspelling ofCaseSensitive(String word, String comment) {
        return Misspelling.of(ReplacementType.MISSPELLING_SIMPLE, word, true, comment);
    }

    private void validateMisspellingWord(String type, String word) {
        if (ReplacementType.MISSPELLING_SIMPLE.equals(type)) {
            boolean isValid = word.chars().allMatch(Character::isLetter);
            if (!isValid) {
                throw new IllegalArgumentException("Not valid misspelling word: " + word);
            }
        }
    }

    private List<Suggestion> parseSuggestionsFromComment(String comment) {
        List<Suggestion> suggestionList = new ArrayList<>(5);

        // EXCEPTION: Composed misspellings with one word finished with comma, e.g. "mas."
        if (comment.endsWith(",") && FinderUtils.isWord(comment.substring(0, comment.length() - 2))) {
            suggestionList.add(Suggestion.ofNoComment(comment));
            return suggestionList;
        }

        Matcher m = PATTERN_SUGGESTION.matcher(comment);
        while (m.find()) {
            String suggestion = m.group().trim();
            suggestionList.add(parseSuggestion(suggestion));
        }

        return suggestionList;
    }

    private Suggestion parseSuggestion(String suggestion) {
        String text = suggestion.replaceAll(REGEX_BRACKETS, "").trim();
        Matcher m = PATTERN_BRACKETS.matcher(suggestion);
        String explanation = null;
        if (m.find()) {
            // Remove the leading and trailing brackets
            String brackets = m.group();
            explanation = brackets.substring(1, brackets.length() - 1).trim();
        }
        return Suggestion.of(text, explanation);
    }
}
