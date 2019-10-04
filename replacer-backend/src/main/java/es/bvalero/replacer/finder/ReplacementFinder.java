package es.bvalero.replacer.finder;

import java.util.List;

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
    default Replacement convertMatch(int start, String text) {
        return Replacement.builder()
                .type(getType())
                .subtype(getSubtype())
                .start(start)
                .text(text)
                .suggestions(findSuggestions(text))
                .build();
    }

    String getType();

    String getSubtype();

    List<ReplacementSuggestion> findSuggestions(String text);

}
