package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.finder.ReplacementSuggestion;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
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
    private static final String REGEX_COMMENT = "([^,(]+)(\\([^)]+\\))?";
    private static final Pattern PATTERN_COMMENT = Pattern.compile(REGEX_COMMENT);

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

        Matcher m = PATTERN_COMMENT.matcher(comment);
        while (m.find()) {
            String text = m.group(1).trim();
            if (StringUtils.isNotBlank(text)) {
                String explanation = StringUtils.isNotBlank(m.group(2))
                        ? m.group(2).substring(1, m.group(2).length() - 1) : ""; // Remove brackets
                suggestionList.add(new ReplacementSuggestion(text, explanation));
            } else {
                throw new IllegalArgumentException("Not valid misspelling comment: " + comment);
            }
        }

        return suggestionList;
    }

}
