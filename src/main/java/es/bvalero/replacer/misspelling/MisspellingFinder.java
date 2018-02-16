package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.PotentialErrorFinder;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.RegexMatchType;
import es.bvalero.replacer.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Finds potential errors of type misspelling in a given text.
 */
@Component
public class MisspellingFinder implements PotentialErrorFinder {

    private static final String REGEX_WORD = "\\b[\\wÁáÉéÍíÓóÚúÜüÑñ]+\\b";

    @Autowired
    private MisspellingManager misspellingManager;

    /**
     * @return A list with the potential errors of type misspelling in a given text.
     * The results are in descendant order by the position.
     */
    @NotNull
    @Override
    public List<ArticleReplacement> findPotentialErrors(@NotNull String text) {
        List<ArticleReplacement> articleReplacements = new LinkedList<>();

        // Find all the words in the text
        List<RegexMatch> textWords = findTextWords(text);

        // For each word, check if it is a known potential misspelling.
        // If so, add it as a replacement for the text.
        for (RegexMatch textWord : textWords) {
            String originalText = textWord.getOriginalText();
            Misspelling wordMisspelling = misspellingManager.findMisspellingByWord(originalText);

            if (wordMisspelling != null && !StringUtils.isAllUppercase(originalText)) {
                // Ignore words all in uppercase except the ones in the misspelling list
                ArticleReplacement replacement = new ArticleReplacement();

                replacement.setPosition(textWord.getPosition());
                replacement.setOriginalText(originalText);
                replacement.setType(RegexMatchType.MISSPELLING);
                replacement.setComment(wordMisspelling.getComment());

                for (String suggestion : wordMisspelling.getSuggestions()) {
                    replacement.getProposedFixes().add(getReplacementFromSuggestion(
                            originalText, suggestion, wordMisspelling.isCaseSensitive()));
                }

                articleReplacements.add(replacement);
            }
        }

        Collections.sort(articleReplacements);

        return articleReplacements;
    }

    @NotNull
    List<RegexMatch> findTextWords(@NotNull String text) {
        return RegExUtils.findMatches(text, REGEX_WORD);
    }

    String getReplacementFromSuggestion(String originalWord, String suggestion, boolean isCaseSensitive) {
        String replacement = suggestion;

        if (StringUtils.startsWithUpperCase(originalWord) && !isCaseSensitive) {
            replacement = StringUtils.setFirstUpperCase(replacement);
        }

        return replacement;
    }

}
