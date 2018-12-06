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

    public String getWord() {
        return word;
    }

    boolean isCaseSensitive() {
        return caseSensitive;
    }

    String getComment() {
        return comment;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Misspelling that = (Misspelling) obj;
        return caseSensitive == that.caseSensitive &&
                Objects.equals(word, that.word) &&
                Objects.equals(comment, that.comment);
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

        public Misspelling build() {
            return new Misspelling(word, caseSensitive, comment);
        }
    }

}
