package es.bvalero.replacer.misspelling;

import java.io.Serializable;
import java.util.List;

class Misspelling implements Serializable {

    private String word;
    private boolean caseSensitive;
    private String comment;
    private List<String> suggestions;

    Misspelling(String word, boolean caseSensitive, String comment, List<String> suggestions) {
        this.word = word;
        this.caseSensitive = caseSensitive;
        this.comment = comment;
        this.suggestions = suggestions;
    }

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

    @Override
    public String toString() {
        return "Misspelling{" +
                "word='" + word + '\'' +
                ", caseSensitive=" + caseSensitive +
                ", comment='" + comment + '\'' +
                ", suggestions=" + suggestions +
                '}';
    }

}
