package es.bvalero.replacer.article.finder;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.PotentialErrorType;
import es.bvalero.replacer.misspelling.Misspelling;
import es.bvalero.replacer.misspelling.MisspellingManager;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds potential errors of type misspelling in a given text.
 */
@Component
public class MisspellingFinder implements PotentialErrorFinder {

    @Autowired
    private MisspellingManager misspellingManager;

    /**
     * @return A list with the potential errors of type misspelling in a given text.
     */
    @NotNull
    @Override
    public List<ArticleReplacement> findPotentialErrors(@NotNull String text) {
        List<ArticleReplacement> articleReplacements = new ArrayList<>(100);

        // Replace any character non-alphanumeric with two spaces
        String cleanText = text.replaceAll("[^\\p{L}\\p{N}]", " ");
        List<RegexMatch> misspellingMatches = RegExUtils.findMatchesAutomaton(cleanText, misspellingManager.getMisspellingAutomaton());

        // For each word, check if it is a known potential misspelling.
        // If so, add it as a replacement for the text.
        for (RegexMatch misspellingMatch : misspellingMatches) {
            // Check if the found word is complete, i. e. check the character before the match
            if (misspellingMatch.getPosition() == 0 || misspellingMatch.getEnd() == cleanText.length()
                    || (cleanText.charAt(misspellingMatch.getPosition() - 1) == ' ' && cleanText.charAt(misspellingMatch.getEnd()) == ' ')) {
                String originalText = misspellingMatch.getOriginalText();
                Misspelling wordMisspelling = misspellingManager.findMisspellingByWord(originalText);

                if (wordMisspelling != null) {
                    ArticleReplacement replacement = new ArticleReplacement();
                    replacement.setPosition(misspellingMatch.getPosition());
                    replacement.setOriginalText(originalText);
                    replacement.setType(PotentialErrorType.MISSPELLING);
                    replacement.setSubtype(wordMisspelling.getWord());
                    replacement.setComment(wordMisspelling.getComment());

                    for (String suggestion : wordMisspelling.getSuggestions()) {
                        replacement.getProposedFixes().add(getReplacementFromSuggestion(
                                originalText, suggestion, wordMisspelling.isCaseSensitive()));
                    }

                    articleReplacements.add(replacement);
                }
            }
        }

        return articleReplacements;
    }

    @NotNull
    String getReplacementFromSuggestion(@NotNull String originalWord, @NotNull String suggestion, boolean isCaseSensitive) {
        String replacement = suggestion;

        if (StringUtils.startsWithUpperCase(originalWord) && !isCaseSensitive) {
            replacement = StringUtils.setFirstUpperCase(replacement);
        }

        return replacement;
    }

}
