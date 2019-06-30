package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.finder.ReplacementSuggestion;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Domain class corresponding to the lines in the Wikipedia article containing potential misspellings.
 */
final class Misspelling {

    private final String word;
    private final boolean caseSensitive;
    private final String comment;

    private final List<ReplacementSuggestion> suggestions;

    private Misspelling(String word, boolean caseSensitive, String comment, List<ReplacementSuggestion> suggestions) {
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

    List<ReplacementSuggestion> getSuggestions() {
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
        @RegExp
        private static final String REGEX_COMMENT = "([^,(]+)(\\([^)]+\\))?";
        private static final Pattern PATTERN_COMMENT = Pattern.compile(REGEX_COMMENT);

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
                LOGGER.info("Ignore not valid misspelling word: {}", word);
                return null;
            }

            List<ReplacementSuggestion> suggestions = new ArrayList<>();
            try {
                suggestions.addAll(parseSuggestionsFromComment(comment));
            } catch (Exception e) {
                LOGGER.error("Error parsing misspelling comment", e);
            }

            if (suggestions.isEmpty()) {
                LOGGER.warn("Not valid misspelling comment: {}", comment);
            }

            return new Misspelling(word, caseSensitive, comment, suggestions);
        }

        private boolean isMisspellingWordValid(String word) {
            return word.chars().allMatch(c -> Character.isLetter(c) || c == '\'' || c == '-');
        }

        private List<ReplacementSuggestion> parseSuggestionsFromComment(String comment) {
            List<ReplacementSuggestion> suggestionList = new ArrayList<>(5);

            Matcher m = PATTERN_COMMENT.matcher(comment);
            while (m.find()) {
                String text = m.group(1).trim();
                if (StringUtils.isNotBlank(text)) {
                    String explanation = StringUtils.isNotBlank(m.group(2))
                            ? m.group(2).substring(1, m.group(2).length() - 1) : ""; // Remove brackets
                    suggestionList.add(new ReplacementSuggestion(text, explanation));
                } else {
                    LOGGER.warn("Not valid misspelling comment: {}", comment);
                }
            }

            return suggestionList;
        }
    }

}
