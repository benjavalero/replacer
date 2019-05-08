package es.bvalero.replacer.misspelling;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Domain class corresponding to the lines in the Wikipedia article containing potential misspellings.
 */
public final class Misspelling {

    private static final Pattern PATTERN_BRACKETS = Pattern.compile("\\(.+?\\)");

    private final String word;
    private final boolean caseSensitive;
    private final String comment;

    private final List<String> suggestions = new ArrayList<>();

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

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    String getComment() {
        return comment;
    }

    List<String> getSuggestions() {
        // Parse only when needed
        if (this.suggestions.isEmpty()) {
            this.suggestions.addAll(parseSuggestionsFromComment());
        }
        return suggestions;
    }

    /**
     * We compare all the fields as maybe only the comment has changed and we want to detect it
     */
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

    private List<String> parseSuggestionsFromComment() {
        List<String> suggestions = new ArrayList<>(5);

        String suggestionNoBrackets = PATTERN_BRACKETS.matcher(getComment()).replaceAll("");
        for (String suggestion : suggestionNoBrackets.split(",")) {
            String suggestionWord = suggestion.trim();

            // Don't suggest the misspelling main word
            if (StringUtils.isNotBlank(suggestionWord) && !suggestionWord.equals(getWord())) {
                suggestions.add(suggestionWord);
            }
        }

        return suggestions;
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
