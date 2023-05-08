package es.bvalero.replacer.finder.listing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;

@Getter
@EqualsAndHashCode
public abstract class Misspelling {

    @RegExp
    private static final String REGEX_BRACKETS = "\\([^)]+\\)";

    private static final Pattern PATTERN_BRACKETS = Pattern.compile(REGEX_BRACKETS);

    @RegExp
    private static final String REGEX_SUGGESTION = String.format("([^,(]|%s)+", REGEX_BRACKETS);

    private static final Pattern PATTERN_SUGGESTION = Pattern.compile(REGEX_SUGGESTION);

    private final String word;
    private final boolean caseSensitive;
    private final List<MisspellingSuggestion> suggestions;

    protected Misspelling(String word, boolean caseSensitive, String comment) {
        this.word = word;
        this.caseSensitive = caseSensitive;
        this.suggestions = parseComment(comment);
    }

    private List<MisspellingSuggestion> parseComment(String comment) {
        List<MisspellingSuggestion> suggestionList = new ArrayList<>();

        try {
            // EXCEPTION: Composed misspellings with one word finished with comma, e.g. "mas."
            if (comment.endsWith(",") && StringUtils.isAlpha(comment.substring(0, comment.length() - 2))) {
                suggestionList.add(MisspellingSuggestion.ofNoComment(comment));
                return suggestionList;
            }

            Matcher m = PATTERN_SUGGESTION.matcher(comment);
            while (m.find()) {
                String suggestion = m.group().trim();
                suggestionList.add(parseSuggestion(suggestion));
            }

            if (suggestionList.isEmpty()) {
                throw new IllegalArgumentException("No suggestions");
            }

            if (suggestionList.size() == 1 && suggestionList.get(0).getText().equals(this.word)) {
                throw new IllegalArgumentException("Only suggestion is equal to the word");
            }

            return suggestionList;
        } catch (Exception e) {
            throw new IllegalArgumentException("Not valid misspelling comment: " + comment, e);
        }
    }

    private MisspellingSuggestion parseSuggestion(String suggestion) {
        String text = suggestion.replaceAll(REGEX_BRACKETS, "").trim();
        Matcher m = PATTERN_BRACKETS.matcher(suggestion);
        String explanation = null;
        if (m.find()) {
            // Remove the leading and trailing brackets
            String brackets = m.group();
            explanation = brackets.substring(1, brackets.length() - 1).trim();
        }
        return MisspellingSuggestion.of(text, explanation);
    }
}
