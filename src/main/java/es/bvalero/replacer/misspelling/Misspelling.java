package es.bvalero.replacer.misspelling;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain class corresponding to the lines in the Wikipedia article containing potential misspellings.
 */
public class Misspelling {

    private String word;
    private boolean caseSensitive;
    private String comment;

    // Derived from the comment. In order not to calculate them every time.
    private List<String> suggestions;

    public Misspelling(String word, boolean caseSensitive, String comment) {
        this.word = word;
        this.caseSensitive = caseSensitive;
        this.comment = comment;
    }

    public String getWord() {
        return word;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public String getComment() {
        return comment;
    }

    public List<String> getSuggestions() {
        if (this.suggestions == null) {
            this.suggestions = parseSuggestionsFromComment();
        }
        return this.suggestions;
    }

    private List<String> parseSuggestionsFromComment() {
        List<String> commentSuggestions = new ArrayList<>();

        String suggestionWithoutBrackets = getComment().replaceAll("\\(.+?\\)", "");
        for (String suggestion : suggestionWithoutBrackets.split(",")) {
            String suggestionWord = suggestion.trim();

            // Don't suggest the misspelling main word
            if (StringUtils.isNotBlank(suggestionWord) && !suggestionWord.equals(getWord())) {
                commentSuggestions.add(suggestionWord);
            }
        }

        return commentSuggestions;
    }

}
