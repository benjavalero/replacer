package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Find misspelling replacements in a given text.
 */
@Component
public class MisspellingFinder implements ArticleReplacementFinder {

    private static final Pattern PATTERN_BRACKETS = Pattern.compile("\\(.+?\\)");

    @Autowired
    private MisspellingManager misspellingManager;

    static String findMisspellingSuggestion(CharSequence originalWord, Misspelling misspelling) {
        List<String> suggestions = parseCommentSuggestions(misspelling);

        // TODO Take into account all the suggestions
        String suggestion = suggestions.get(0);

        if (MisspellingManager.startsWithUpperCase(originalWord) && !misspelling.isCaseSensitive()) {
            suggestion = MisspellingManager.setFirstUpperCase(suggestion);
        }

        return suggestion;
    }

    static List<String> parseCommentSuggestions(Misspelling misspelling) {
        List<String> suggestions = new ArrayList<>(5);

        String suggestionNoBrackets = PATTERN_BRACKETS.matcher(misspelling.getComment()).replaceAll("");
        for (String suggestion : suggestionNoBrackets.split(",")) {
            String suggestionWord = suggestion.trim();

            // Don't suggest the misspelling main word
            if (StringUtils.isNotBlank(suggestionWord) && !suggestionWord.equals(misspelling.getWord())) {
                suggestions.add(suggestionWord);
            }
        }

        return suggestions;
    }

    /**
     * @return A list with the misspelling replacements in a given text.
     */
    @Override
    public List<ArticleReplacement> findReplacements(String text) {
        List<ArticleReplacement> articleReplacements = new ArrayList<>(100);

        List<ArticleReplacement> misspellingMatches = ArticleReplacementFinder.findReplacements(text,
                misspellingManager.getMisspellingAutomaton(), ReplacementType.MISSPELLING);

        for (ArticleReplacement misspellingMatch : misspellingMatches) {
            // The regex may find misspellings which are not complete words, e. g. "és" inside "inglés"
            //noinspection OverlyComplexBooleanExpression
            if (misspellingMatch.getStart() == 0 || misspellingMatch.getEnd() == text.length() ||
                    (!Character.isLetterOrDigit(text.charAt(misspellingMatch.getStart() - 1))
                            && !Character.isLetterOrDigit(text.charAt(misspellingMatch.getEnd())))) {
                Misspelling wordMisspelling = misspellingManager.findMisspellingByWord(misspellingMatch.getText());

                articleReplacements.add(misspellingMatch
                        .withSubtype(wordMisspelling.getWord())
                        .withComment(wordMisspelling.getComment())
                        .withSuggestion(findMisspellingSuggestion(misspellingMatch.getText(), wordMisspelling)));
            }
        }

        return articleReplacements;
    }

}
