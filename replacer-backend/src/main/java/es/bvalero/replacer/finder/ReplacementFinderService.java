package es.bvalero.replacer.finder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class ReplacementFinderService {

    @Autowired
    private List<ArticleReplacementFinder> articleReplacementFinders;

    @Autowired
    private List<IgnoredReplacementFinder> ignoredReplacementFinders;

    /**
     * @param text The text to find replacements in.
     * @return A list with all the replacements in the text.
     * Replacements contained in exceptions are ignored.
     * If there are no replacements, the list will be empty.
     */
    public List<ArticleReplacement> findReplacements(String text) {
        // Find the replacements in the text
        // LinkedList is better to run iterators and remove items from it
        List<ArticleReplacement> articleReplacements = new LinkedList<>();
        for (ArticleReplacementFinder finder : articleReplacementFinders) {
            articleReplacements.addAll(finder.findReplacements(text));
        }

        // No need to find the exceptions if there are no replacements found
        if (articleReplacements.isEmpty()) {
            return articleReplacements;
        }

        // Ignore the replacements which must be ignored
        for (IgnoredReplacementFinder ignoredFinder : ignoredReplacementFinders) {
            List<MatchResult> ignoredReplacements = ignoredFinder.findIgnoredReplacements(text);
            articleReplacements.removeIf(replacement -> replacement.isContainedIn(ignoredReplacements));

            if (articleReplacements.isEmpty()) {
                break;
            }
        }

        return articleReplacements;
    }

}