package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.PotentialErrorFinder;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.RegexMatchType;
import es.bvalero.replacer.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Component
public class MisspellingFinder implements PotentialErrorFinder {

    private static final String REGEX_WORD = "\\b[\\wÁáÉéÍíÓóÚúÜüÑñ]+\\b";

    @Autowired
    private MisspellingManager misspellingManager;

    @Override
    public List<ArticleReplacement> findPotentialErrors(String text) {
        List<ArticleReplacement> articleReplacements = new LinkedList<>();

        // Find all the words in the text
        List<RegexMatch> textWords = findTextWords(text);

        // For each word, check if it is a known potential misspelling.
        // If so, add it as a replacement for the text.
        for (RegexMatch textWord : textWords) {
            String word = textWord.getOriginalText();
            Misspelling wordMisspelling = misspellingManager.findMisspellingByWord(word);
            // Ignore words all in uppercase except the ones in the misspelling list
            if (wordMisspelling != null
                    && (!org.apache.commons.lang3.StringUtils.isAllUpperCase(word)
                        || misspellingManager.isUppercaseMisspelling(word))) {
                ArticleReplacement replacement = new ArticleReplacement();
                replacement.setPosition(textWord.getPosition());
                replacement.setOriginalText(wordMisspelling.getWord());
                replacement.setType(RegexMatchType.MISSPELLING);
                replacement.setProposedFixes(findProposedFixes(word, wordMisspelling));
                replacement.setComment(wordMisspelling.getComment());
                articleReplacements.add(replacement);
            }
        }

        Collections.sort(articleReplacements);
        return articleReplacements;
    }

    List<RegexMatch> findTextWords(String text) {
        return RegExUtils.findMatches(text, REGEX_WORD);
    }

    List<String> findProposedFixes(String word, Misspelling misspelling) {
        List<String> proposedFixes = new ArrayList<>();

        for (final String suggestion : misspelling.getSuggestions()) {
            String proposedFix =
                    (!misspelling.isCaseSensitive() && StringUtils.startsWithUpperCase(word))
                            ? StringUtils.setFirstUpperCase(suggestion)
                            : suggestion;
            proposedFixes.add(proposedFix);
        }

        return proposedFixes;
    }

}
