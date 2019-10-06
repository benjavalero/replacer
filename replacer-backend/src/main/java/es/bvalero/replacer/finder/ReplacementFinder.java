package es.bvalero.replacer.finder;

import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;

/**
 * Classes implementing this interface will provide methods to find potential replacements of different types.
 */
public interface ReplacementFinder extends BaseFinder<Replacement> {

    /**
     * @return A list of potential replacements in the text.
     */
    List<Replacement> findReplacements(String text);

    @Override
    default boolean isValidMatch(int start, String matchedText, String fullText) {
        return FinderUtils.isWordCompleteInText(start, matchedText, fullText);
    }

    @Override
    default Replacement convertMatch(MatchResult matcher) {
        int start = matcher.start();
        String text = matcher.group();
        return Replacement.builder()
                .type(getType())
                .subtype(getSubtype(text))
                .start(start)
                .text(text)
                .suggestions(findSuggestions(matcher))
                .build();
    }

    @Override
    default Replacement convertMatch(int start, String text) {
        return Replacement.builder()
                .type(getType())
                .subtype(getSubtype(text))
                .start(start)
                .text(text)
                .suggestions(findSuggestions(text))
                .build();
    }

    String getType();

    String getSubtype(String text);

    default List<ReplacementSuggestion> findSuggestions(MatchResult matcher) {
        return findSuggestions(matcher.group());
    }

    default List<ReplacementSuggestion> findSuggestions(String text) {
        return Collections.emptyList();
    }

}
