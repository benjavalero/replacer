package es.bvalero.replacer.finder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class ReplacementFinderService {

    public static final String CUSTOM_FINDER_TYPE = "Personalizado";

    @Autowired
    private List<ArticleReplacementFinder> articleReplacementFinders;

    @Autowired
    private List<IgnoredReplacementFinder> ignoredReplacementFinders;

    @Autowired
    private CustomReplacementFinder customReplacementFinder;

    /**
     * @param text The text to find replacements in.
     * @return A list with all the replacements in the text.
     * Replacements contained in exceptions are ignored.
     * If there are no replacements, the list will be empty.
     */
    public List<ArticleReplacement> findReplacements(String text) {
        LOGGER.debug("START Find replacements in text: {}", text);
        // Find the replacements in the text
        // LinkedList is better to run iterators and remove items from it
        List<ArticleReplacement> articleReplacements = new LinkedList<>();
        for (ArticleReplacementFinder finder : articleReplacementFinders) {
            LOGGER.debug("- START Find replacements of type: {}", finder.getType());
            List<ArticleReplacement> replacements = finder.findReplacements(text);
            LOGGER.debug("- END Find replacements of type: {}", finder.getType());
            articleReplacements.addAll(replacements);
        }
        LOGGER.debug("Potential replacements found (before ignoring): {} - {}",
                articleReplacements.size(), articleReplacements);

        // Remove nested replacements
        articleReplacements.removeIf(replacement -> isReplacementContainedInListIgnoringItself(replacement, articleReplacements));
        LOGGER.debug("Potential replacements found after removing nested: {} - {}",
                articleReplacements.size(), articleReplacements);

        // No need to find the exceptions if there are no replacements found
        if (articleReplacements.isEmpty()) {
            return articleReplacements;
        }

        // Ignore the replacements which must be ignored
        for (IgnoredReplacementFinder ignoredFinder : ignoredReplacementFinders) {
            LOGGER.debug("- START Find ignored of type: {}", ignoredFinder.getClass().getSimpleName());
            List<MatchResult> ignoredReplacements = ignoredFinder.findIgnoredReplacements(text);
            LOGGER.debug("- Found ignored of type: {}", ignoredReplacements);
            LOGGER.debug("- END Find ignored of type: {}", ignoredFinder.getClass().getSimpleName());
            articleReplacements.removeIf(replacement -> isReplacementContainedInMatchResultList(replacement, ignoredReplacements));

            if (articleReplacements.isEmpty()) {
                break;
            }
        }

        LOGGER.debug("END Find replacements in text. Final replacements found: {} - {}",
                articleReplacements.size(), articleReplacements);
        return articleReplacements;
    }

    boolean isReplacementContainedInListIgnoringItself(ArticleReplacement replacement, List<ArticleReplacement> replacementList) {
        boolean isContained = false;
        for (ArticleReplacement replacementItem : replacementList) {
            if (isReplacementContainedInReplacement(replacement, replacementItem)) {
                isContained = true;
                break;
            }
        }
        return isContained;
    }

    private boolean isReplacementContainedInReplacement(ArticleReplacement replacement1, ArticleReplacement replacement2) {
        return !replacement1.equals(replacement2) && isIntervalContainedInInterval(
                replacement1.getStart(), replacement1.getEnd(),
                replacement2.getStart(), replacement2.getEnd());
    }

    private boolean isIntervalContainedInInterval(int start1, int end1, int start2, int end2) {
        return start1 >= start2 && end1 <= end2;
    }

    private boolean isReplacementContainedInMatchResultList(ArticleReplacement replacement, List<MatchResult> matchResults) {
        return matchResults.stream().anyMatch(match -> isReplacementContainedInMatchResult(replacement, match));
    }

    private boolean isReplacementContainedInMatchResult(ArticleReplacement replacement, MatchResult matchResult) {
        return isIntervalContainedInInterval(replacement.getStart(), replacement.getEnd(), matchResult.getStart(), matchResult.getEnd());
    }

    public List<ArticleReplacement> findCustomReplacements(String text, String replacement, String suggestion) {
        LOGGER.debug("START Find custom replacements. Text: {} - Replacement: {} - Suggestion: {}",
                text, replacement, suggestion);
        List<ArticleReplacement> articleReplacements = customReplacementFinder.findReplacements(text, replacement, suggestion);
        LOGGER.debug("Potential custom replacements found (before ignoring): {}", articleReplacements);

        // No need to find the exceptions if there are no replacements found
        if (articleReplacements.isEmpty()) {
            return articleReplacements;
        }

        // Ignore the replacements which must be ignored
        for (IgnoredReplacementFinder ignoredFinder : ignoredReplacementFinders) {
            List<MatchResult> ignoredReplacements = ignoredFinder.findIgnoredReplacements(text);
            articleReplacements.removeIf(artRep -> isReplacementContainedInMatchResultList(artRep, ignoredReplacements));

            if (articleReplacements.isEmpty()) {
                break;
            }
        }

        LOGGER.debug("END Find custom replacements in text. Final replacements found: {}", articleReplacements);
        return articleReplacements;
    }

}