package es.bvalero.replacer.misspelling;

import java.util.Objects;

/**
 * Domain class corresponding to the lines in the Wikipedia article containing potential misspellings.
 */
final class Misspelling {

    private final String word;
    private final boolean caseSensitive;
    private final String comment;

    private Misspelling(String word, boolean caseSensitive, String comment) {
        this.word = word;
        this.caseSensitive = caseSensitive;
        this.comment = comment;
    }

    static Misspelling.MisspellingBuilder builder() {
        return new Misspelling.MisspellingBuilder();
    }

    String getWord() {
        return word;
    }

    boolean isCaseSensitive() {
        return caseSensitive;
    }

    String getComment() {
        return comment;
    }

    /** We compare all the fields as maybe only the comment has changed and we want to detect it */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Misspelling that = (Misspelling) o;
        return caseSensitive == that.caseSensitive &&
                word.equals(that.word) &&
                comment.equals(that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, caseSensitive, comment);
    }

    @Override
    public String toString() {
        return "Misspelling{" +
                "word='" + word + '\'' +
                ", caseSensitive=" + caseSensitive +
                ", comment='" + comment + '\'' +
                '}';
    }

    static class MisspellingBuilder {
        private String word;
        private boolean caseSensitive;
        private String comment;

        Misspelling.MisspellingBuilder setWord(String word) {
            this.word = word;
            return this;
        }

        Misspelling.MisspellingBuilder setCaseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        Misspelling.MisspellingBuilder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        Misspelling build() {
            return new Misspelling(word, caseSensitive, comment);
        }
    }

}
