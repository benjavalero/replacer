package es.bvalero.replacer.misspelling;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Domain class corresponding to the lines in the Wikipedia article containing potential misspellings.
 */
final class Misspelling {

    private final String word;
    private final boolean caseSensitive;
    private final String comment;

    private final List<String> suggestions;

    private Misspelling(String word, boolean caseSensitive, String comment, List<String> suggestions) {
        this.word = word;
        this.caseSensitive = caseSensitive;
        this.comment = comment;
        this.suggestions = suggestions;
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

    List<String> getSuggestions() {
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

    static class MisspellingBuilder {
        private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingBuilder.class);
        private static final Pattern PATTERN_BRACKETS = Pattern.compile("\\(.+?\\)");

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

        @Nullable Misspelling build() {
            if (!isMisspellingWordValid(word)) {
                LOGGER.warn("Misspelling word not valid: {}. Skipping.", word);
                return null;
            }

            List<String> suggestions = parseSuggestionsFromComment(word, comment);
            if (suggestions.isEmpty()) {
                LOGGER.warn("Misspelling comment not valid: {}. Word: {}. Skipping.", comment, word);
            }

            return new Misspelling(word, caseSensitive, comment, suggestions);
        }

        private boolean isMisspellingWordValid(String word) {
            return word.chars().allMatch(c -> Character.isLetter(c) || c == '\'' || c == '-');
        }

        private List<String> parseSuggestionsFromComment(String word, String comment) {
            List<String> suggestionList = new ArrayList<>(5);

            String suggestionNoBrackets = PATTERN_BRACKETS.matcher(comment).replaceAll("");
            for (String suggestion : suggestionNoBrackets.split(",")) {
                String suggestionWord = suggestion.trim();

                // Don't suggest the misspelling main word
                if (StringUtils.isNotBlank(suggestionWord) && !suggestionWord.equals(word)) {
                    suggestionList.add(suggestionWord);
                }
            }

            return suggestionList;
        }
    }

}
