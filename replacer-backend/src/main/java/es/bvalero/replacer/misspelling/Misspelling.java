package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.finder.ReplacementSuggestion;
import lombok.Value;
import org.intellij.lang.annotations.RegExp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Domain class corresponding to the lines in the Wikipedia article containing potential misspellings.
 */
@Value
class Misspelling {
    @RegExp
    private static final String REGEX_BRACKETS = "\\([^)]+\\)";
    private static final Pattern PATTERN_BRACKETS = Pattern.compile(REGEX_BRACKETS);
    @RegExp
    private static final String REGEX_SUGGESTION = String.format("([^,(]|%s)+", REGEX_BRACKETS);
    private static final Pattern PATTERN_SUGGESTION = Pattern.compile(REGEX_SUGGESTION);

    private String word;
    private boolean caseSensitive;
    private List<ReplacementSuggestion> suggestions;

    private Misspelling(String word, boolean caseSensitive, String comment) {
        // Validate the word
        if (!isMisspellingWordValid(word)) {
            throw new IllegalArgumentException("Not valid misspelling word: " + word);
        }
        this.word = word;
        this.caseSensitive = caseSensitive;

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

    static Misspelling of(String word, boolean caseSensitive, String comment) {
        return new Misspelling(word, caseSensitive, comment);
    }

    static Misspelling ofCaseInsensitive(String word, String comment) {
        return Misspelling.of(word, false, comment);
    }

    private boolean isMisspellingWordValid(String word) {
        return word.chars().allMatch(c -> Character.isLetter(c) || c == '\'' || c == '-');
    }

    private List<ReplacementSuggestion> parseSuggestionsFromComment(String comment) {
        List<ReplacementSuggestion> suggestionList = new ArrayList<>(5);

        Matcher m = PATTERN_SUGGESTION.matcher(comment);
        while (m.find()) {
            String suggestion = m.group().trim();
            suggestionList.add(parseSuggestion(suggestion));
        }

        return suggestionList;
    }

    private ReplacementSuggestion parseSuggestion(String suggestion) {
        String text = suggestion.replaceAll(REGEX_BRACKETS, "").trim();
        Matcher m = PATTERN_BRACKETS.matcher(suggestion);
        String explanation = "";
        if (m.find()) {
            // Remove the leading and trailing brackets
            String brackets = m.group();
            explanation = brackets.substring(1, brackets.length() - 1).trim();
        }
        return ReplacementSuggestion.of(text, explanation);
    }

}
