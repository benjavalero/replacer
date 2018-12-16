package es.bvalero.replacer.misspelling;

import java.util.Objects;

/**
 * Domain class corresponding to the lines in the Wikipedia article containing potential misspellings.
 */
public final class Misspelling {

    private final String word;
    private final boolean caseSensitive;
    private final String comment;

    private Misspelling(String word, boolean caseSensitive, String comment) {
        this.word = word;
        this.caseSensitive = caseSensitive;
        this.comment = comment;
    }

    public static Misspelling.MisspellingBuilder builder() {
        return new Misspelling.MisspellingBuilder();
    }

    public String getWord() {
        return word;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    String getComment() {
        return comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Misspelling that = (Misspelling) o;
        return caseSensitive == that.caseSensitive &&
                word.equals(that.word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, caseSensitive);
    }

    @Override
    public String toString() {
        return "Misspelling{" +
                "word='" + word + '\'' +
                ", caseSensitive=" + caseSensitive +
                ", comment='" + comment + '\'' +
                '}';
    }

    public static class MisspellingBuilder {
        private String word;
        private boolean caseSensitive;
        private String comment;

        public Misspelling.MisspellingBuilder setWord(String word) {
            this.word = word;
            return this;
        }

        public Misspelling.MisspellingBuilder setCaseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        Misspelling.MisspellingBuilder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Misspelling build() {
            return new Misspelling(word, caseSensitive, comment);
        }
    }

}
