package es.bvalero.replacer.finder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class ReplacementFinderService {

    public static final String CUSTOM_FINDER_TYPE = "Personalizado";

    @Autowired
    private List<ReplacementFinder> replacementFinders;

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
    public List<Replacement> findReplacements(String text) {
        LOGGER.debug("START Find replacements in text: {}", text);
        // Find the replacements in the text
        // LinkedList is better to run iterators and remove items from it
        List<Replacement> replacements = new LinkedList<>();
        for (ReplacementFinder finder : replacementFinders) {
            LOGGER.debug("- START Find replacements of type: {}", finder.getClass().getSimpleName());
            List<Replacement> finderReplacements = finder.findReplacements(text);
            LOGGER.debug("- END Find {} replacements of type: {}", finderReplacements.size(), finder.getClass().getSimpleName());
            replacements.addAll(finderReplacements);
        }
        LOGGER.debug("Potential replacements found (before ignoring): {} - {}",
                replacements.size(), replacements);

        // Remove nested replacements
        Collections.sort(replacements);
        replacements.removeIf(replacement -> isReplacementContainedInListIgnoringItself(replacement, replacements));
        LOGGER.debug("Potential replacements found after removing nested: {} - {}",
                replacements.size(), replacements);

        // No need to find the exceptions if there are no replacements found
        if (replacements.isEmpty()) {
            return replacements;
        }

        // Ignore the replacements which must be ignored
        removeIgnoredReplacements(text, replacements);

        LOGGER.debug("END Find replacements in text. Final replacements found: {} - {}",
                replacements.size(), replacements);
        return replacements;
    }

    boolean isReplacementContainedInListIgnoringItself(Replacement replacement, List<Replacement> replacementList) {
        boolean isContained = false;
        for (Replacement replacementItem : replacementList) {
            if (isReplacementContainedInReplacement(replacement, replacementItem)) {
                isContained = true;
                break;
            }
        }
        return isContained;
    }

    private boolean isReplacementContainedInReplacement(Replacement replacement1, Replacement replacement2) {
        return !replacement1.equals(replacement2) && isIntervalContainedInInterval(
                replacement1.getStart(), replacement1.getEnd(),
                replacement2.getStart(), replacement2.getEnd());
    }

    private boolean isIntervalContainedInInterval(int start1, int end1, int start2, int end2) {
        return start1 >= start2 && end1 <= end2;
    }

    private void removeIgnoredReplacements(String text, List<Replacement> replacements) {
        for (IgnoredReplacementFinder ignoredFinder : ignoredReplacementFinders) {
            LOGGER.debug("- START Find ignored of type: {}", ignoredFinder.getClass().getSimpleName());
            List<MatchResult> ignoredReplacements = ignoredFinder.findIgnoredReplacements(text);
            LOGGER.debug("- Found ignored of type: {}", ignoredReplacements);
            LOGGER.debug("- END Find ignored of type: {}", ignoredFinder.getClass().getSimpleName());
            replacements.removeIf(replacement -> isReplacementContainedInMatchResultList(replacement, ignoredReplacements));

            if (replacements.isEmpty()) {
                break;
            }
        }
    }

    private boolean isReplacementContainedInMatchResultList(Replacement replacement, List<MatchResult> matchResults) {
        return matchResults.stream().anyMatch(match -> isReplacementContainedInMatchResult(replacement, match));
    }

    private boolean isReplacementContainedInMatchResult(Replacement replacement, MatchResult matchResult) {
        return isIntervalContainedInInterval(replacement.getStart(), replacement.getEnd(), matchResult.getStart(), matchResult.getEnd());
    }

    public List<Replacement> findCustomReplacements(String text, String replacement, String suggestion) {
        LOGGER.debug("START Find custom replacements. Text: {} - Replacement: {} - Suggestion: {}",
                text, replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findReplacements(text, replacement, suggestion);
        LOGGER.debug("Potential custom replacements found (before ignoring): {}", replacements);

        // No need to find the exceptions if there are no replacements found
        if (replacements.isEmpty()) {
            return replacements;
        }

        // Ignore the replacements which must be ignored
        for (IgnoredReplacementFinder ignoredFinder : ignoredReplacementFinders) {
            List<MatchResult> ignoredReplacements = ignoredFinder.findIgnoredReplacements(text);
            replacements.removeIf(artRep -> isReplacementContainedInMatchResultList(artRep, ignoredReplacements));

            if (replacements.isEmpty()) {
                break;
            }
        }

        LOGGER.debug("END Find custom replacements in text. Final replacements found: {}", replacements);
        return replacements;
    }

}