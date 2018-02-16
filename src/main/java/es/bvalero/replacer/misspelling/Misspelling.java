package es.bvalero.replacer.misspelling;

import java.util.List;

/**
 * Domain class corresponding to the lines in the Wikipedia article containing potential misspellings.
 */
class Misspelling {

    private String word;
    private boolean caseSensitive;
    private String comment;

    // Derived from the comment. In order not to calculate them every time.
    private List<String> suggestions;

    String getWord() {
        return word;
    }

    void setWord(String word) {
        this.word = word;
    }

    boolean isCaseSensitive() {
        return caseSensitive;
    }

    void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    String getComment() {
        return comment;
    }

    void setComment(String comment) {
        this.comment = comment;
    }

    List<String> getSuggestions() {
        return suggestions;
    }

    void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

}
