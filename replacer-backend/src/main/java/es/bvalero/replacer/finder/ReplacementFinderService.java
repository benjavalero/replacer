package es.bvalero.replacer.finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class ReplacementFinderService {

    public static final String CUSTOM_FINDER_TYPE = "Personalizado";
    private static final Logger LOGGER = LoggerFactory.getLogger(ReplacementFinderService.class);

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
        articleReplacements.removeIf(replacement -> replacement.isContainedInListSelfIgnoring(articleReplacements));
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
            LOGGER.debug("- END Find ignored of type: {}", ignoredFinder.getClass().getSimpleName());
            articleReplacements.removeIf(replacement -> replacement.isContainedIn(ignoredReplacements));

            if (articleReplacements.isEmpty()) {
                break;
            }
        }

        LOGGER.debug("END Find replacements in text. Final replacements found: {} - {}",
                articleReplacements.size(), articleReplacements);
        return articleReplacements;
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
            articleReplacements.removeIf(artRep -> artRep.isContainedIn(ignoredReplacements));

            if (articleReplacements.isEmpty()) {
                break;
            }
        }

        LOGGER.debug("END Find custom replacements in text. Final replacements found: {}", articleReplacements);
        return articleReplacements;
    }

}