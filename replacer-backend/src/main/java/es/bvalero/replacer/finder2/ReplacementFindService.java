package es.bvalero.replacer.finder2;

import es.bvalero.replacer.finder.CustomReplacementFinder;
import es.bvalero.replacer.finder.Replacement;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Independent service that finds all the replacements in a given text.
 *
 * It is composed by several specific replacement finders: misspelling, date format, etc.
 * which implement the same interface.
 *
 * The service applies all these specific finders and returns the collected results.
 */
@Slf4j
@Service
public class ReplacementFindService {
    @Autowired
    private List<ReplacementFinder> replacementFinders;

    @Autowired
    private ImmutableFindService immutableFindService;

    public Stream<Replacement> findReplacements(String text) {
        // The replacement finder ignores in the response all the found replacements which are contained
        // in the found immutables. Usually there will be much more immutables found than replacements.
        // Thus it is better to obtain first all the replacements, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        LOGGER.debug("START Find replacements in text: {}", text);

        Stream<Replacement> allReplacements = findAllReplacements(text);

        // Ignore the replacements contained in immutables
        Stream<Replacement> filtered = removeImmutables(allReplacements, text).sorted();

        LOGGER.debug("END Find replacements in text. Final replacements found: {}", filtered);
        return filtered;
    }

    public Stream<Replacement> findCustomReplacements(String text, String replacement, String suggestion) {
        LOGGER.debug(
            "START Find custom replacements. Text: {} - Replacement: {} - Suggestion: {}",
            text,
            replacement,
            suggestion
        );

        // Find the replacements in the text
        Stream<Replacement> allReplacements = findAllCustomReplacements(text, replacement, suggestion);

        // Ignore the replacements contained in immutables
        Stream<Replacement> filtered = removeImmutables(allReplacements, text).sorted();

        LOGGER.debug("END Find replacements in text. Final replacements found: {}", filtered);
        return filtered;
    }

    private Stream<Replacement> findAllReplacements(String text) {
        Stream<Replacement> replacements = replacementFinders
            .stream()
            .map(finder -> finder.findReplacements(text))
            .flatMap(s -> s);
        return removeNestedReplacements(replacements);
    }

    private Stream<Replacement> findAllCustomReplacements(String text, String replacement, String suggestion) {
        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        Stream<Replacement> replacements = customReplacementFinder.findReplacements(text).stream();
        return removeNestedReplacements(replacements);
    }

    private Stream<Replacement> removeNestedReplacements(Stream<Replacement> replacements) {
        // We need to filter the stream items against the stream itself so it is not a stateless predicate
        // We assume all the replacements in the stream are distinct, in this case,
        // this means there are not two replacements with the same start and end,
        // so the contain function is strict.
        return replacements.distinct().filter(r -> replacements.anyMatch(r2 -> r2.contains(r)));
    }

    private Stream<Replacement> removeImmutables(Stream<Replacement> replacements, String text) {
        // Build a list from the stream and remove the items contained in any immutable
        List<Replacement> replacementList = replacements.collect(Collectors.toList());

        // No need to find the immutables if there are no replacements
        if (replacementList.isEmpty()) {
            return Stream.empty();
        }

        for (Immutable immutable : immutableFindService.findImmutables(text)) {
            replacementList.removeIf(r -> immutable.contains(r));

            // No need to continue finding the immutables if there are no replacements
            if (replacementList.isEmpty()) {
                return Stream.empty();
            }
        }

        return replacementList.stream();
    }
}
