package es.bvalero.replacer.domain;

import java.io.Serializable;

public class Misspelling implements Serializable {

    private String word;
    private boolean caseSensitive;
    private String suggestion;

    public Misspelling() {
        // Empty constructor
    }

    public Misspelling(String word, boolean caseSensitive, String suggestion) {
        this.word = word;
        this.caseSensitive = caseSensitive;
        this.suggestion = suggestion;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Misspelling that = (Misspelling) o;

        return word.equals(that.word);
    }

    @Override
    public int hashCode() {
        return word.hashCode();
    }

    @Override
    public String toString() {
        return "Misspelling{" +
                "word='" + word + '\'' +
                ", caseSensitive=" + caseSensitive +
                ", suggestion='" + suggestion + '\'' +
                '}';
    }

}
