package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;

@Getter
@EqualsAndHashCode
public abstract class Misspelling {

    @RegExp
    private static final String REGEX_BRACKETS = "\\([^)]+\\)";

    private static final Pattern PATTERN_BRACKETS = Pattern.compile(REGEX_BRACKETS);

    @RegExp
    private static final String REGEX_SUGGESTION = String.format("([^,(]|%s)+", REGEX_BRACKETS);

    private static final Pattern PATTERN_SUGGESTION = Pattern.compile(REGEX_SUGGESTION);

    private final String word;
    private final boolean caseSensitive;
    private final List<MisspellingSuggestion> suggestions;

    // Singleton property only when requested
    // Store the terms covered by the word
    private final Set<String> terms = new HashSet<>();

    protected Misspelling(String word, boolean caseSensitive, String comment) {
        this.word = word;
        this.caseSensitive = caseSensitive;
        this.suggestions = parseComment(comment);
    }

    private List<MisspellingSuggestion> parseComment(String comment) {
        List<MisspellingSuggestion> suggestionList = new ArrayList<>();

        try {
            // EXCEPTION: Composed misspellings with one word finished with comma, e.g. "mas."
            if (comment.endsWith(",") && StringUtils.isAlpha(comment.substring(0, comment.length() - 2))) {
                suggestionList.add(MisspellingSuggestion.ofNoComment(comment));
                return suggestionList;
            }

            Matcher m = PATTERN_SUGGESTION.matcher(comment);
            while (m.find()) {
                String suggestion = m.group().trim();
                suggestionList.add(parseSuggestion(suggestion));
            }

            if (suggestionList.isEmpty()) {
                throw new IllegalArgumentException("No suggestions");
            }

            if (suggestionList.size() == 1 && suggestionList.getFirst().getText().equals(this.word)) {
                throw new IllegalArgumentException("Only suggestion is equal to the word");
            }

            return suggestionList;
        } catch (Exception e) {
            throw new IllegalArgumentException("Not valid misspelling comment: " + comment, e);
        }
    }

    private MisspellingSuggestion parseSuggestion(String suggestion) {
        String text = suggestion.replaceAll(REGEX_BRACKETS, "").trim();
        Matcher m = PATTERN_BRACKETS.matcher(suggestion);
        String explanation = null;
        if (m.find()) {
            // Remove the leading and trailing brackets
            String brackets = m.group();
            explanation = brackets.substring(1, brackets.length() - 1).trim();
        }
        return MisspellingSuggestion.of(text, explanation);
    }

    public Set<String> getTerms() {
        if (terms.isEmpty()) {
            if (isCaseSensitive()) {
                terms.add(word);
            } else {
                // If case-insensitive, we add "word" and "Word".
                String[] tokens = word.split("(?U)\\b");
                addTerms(tokens, 0);
            }
        }
        return terms;
    }

    private void addTerms(String[] tokens, int pos) {
        if (pos >= tokens.length) {
            return;
        }

        String[] lowercase = tokens.clone();
        lowercase[pos] = FinderUtils.setFirstLowerCase(lowercase[pos]);
        terms.add(StringUtils.join(lowercase));
        addTerms(lowercase, pos + 1);

        String[] uppercase = tokens.clone();
        uppercase[pos] = ReplacerUtils.setFirstUpperCase(uppercase[pos]);
        terms.add(StringUtils.join(uppercase));
        addTerms(uppercase, pos + 1);
    }
}
