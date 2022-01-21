package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.RegExp;

public interface Misspelling extends ListingItem {
    @Slf4j
    final class LogHolder {
        // Trick to be able to log in interfaces
    }

    @RegExp
    String REGEX_BRACKETS = "\\([^)]+\\)";

    Pattern PATTERN_BRACKETS = Pattern.compile(REGEX_BRACKETS);

    @RegExp
    String REGEX_SUGGESTION = String.format("([^,(]|%s)+", REGEX_BRACKETS);

    Pattern PATTERN_SUGGESTION = Pattern.compile(REGEX_SUGGESTION);

    String getWord();

    boolean isCaseSensitive();

    List<MisspellingSuggestion> getSuggestions();

    default ReplacementType getReplacementType() {
        ReplacementKind kind = getSuggestions().size() == 1 ? ReplacementKind.SIMPLE : ReplacementKind.COMPLEX;
        return ReplacementType.of(kind, getWord());
    }

    default void validateWordCase() {
        if (!isCaseSensitive() && FinderUtils.startsWithUpperCase(getWord())) {
            LogHolder.LOGGER.warn("Case-insensitive uppercase misspelling: " + getWord());
        }
    }

    default List<MisspellingSuggestion> parseComment(String comment) {
        List<MisspellingSuggestion> suggestionList = new ArrayList<>();

        try {
            // EXCEPTION: Composed misspellings with one word finished with comma, e.g. "mas."
            if (comment.endsWith(",") && FinderUtils.isWord(comment.substring(0, comment.length() - 2))) {
                suggestionList.add(MisspellingSuggestion.ofNoComment(comment));
                return suggestionList;
            }

            Matcher m = PATTERN_SUGGESTION.matcher(comment);
            while (m.find()) {
                String suggestion = m.group().trim();
                suggestionList.add(parseSuggestion(suggestion));
            }

            if (suggestionList.isEmpty()) {
                throw new IllegalArgumentException();
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
}
