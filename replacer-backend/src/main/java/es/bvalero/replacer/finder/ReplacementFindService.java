package es.bvalero.replacer.finder;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Independent service that finds all the replacements in a given text.
 * <p>
 * It is composed by several specific replacement finders: misspelling, date format, etc.
 * which implement the same interface.
 * <p>
 * The service applies all these specific finders and returns the collected results.
 */
@Slf4j
@Service
public class ReplacementFindService {
    private static final int CONTEXT_THRESHOLD = 20;

    @Autowired
    private List<ReplacementFinder> replacementFinders;

    @Autowired
    private ImmutableFindService immutableFindService;

    @Value("${replacer.show.long.immutables}")
    private boolean showLongImmutables;

    @Loggable(prepend = true, value = Loggable.TRACE, unit = TimeUnit.SECONDS)
    public List<Replacement> findReplacements(String text, WikipediaLanguage lang) {
        // The replacement finder ignores in the response all the found replacements which are contained
        // in the found immutables. Usually there will be much more immutables found than replacements.
        // Thus it is better to obtain first all the replacements, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        return findFilteredReplacements(text, lang, replacementFinders);
    }

    @Loggable(prepend = true, value = Loggable.TRACE, unit = TimeUnit.SECONDS)
    public List<Replacement> findCustomReplacements(
        String text,
        String replacement,
        String suggestion,
        WikipediaLanguage lang
    ) {
        CustomReplacementFinder finder = new CustomReplacementFinder(replacement, suggestion);
        return findFilteredReplacements(text, lang, Collections.singletonList(finder));
    }

    /* Find all the replacements in the text but return only the necessary */
    private List<Replacement> findFilteredReplacements(
        String text,
        WikipediaLanguage lang,
        List<ReplacementFinder> finders
    ) {
        Set<Replacement> all = findSortedReplacements(text, lang, finders);

        Stream<Replacement> notNested = removeNestedReplacements(all);

        // Ignore the replacements contained in immutables
        List<Replacement> noIgnored = removeImmutables(notNested, text, lang);

        return addContextToReplacements(noIgnored, text);
    }

    /* Find all the replacements in the text sorted and without duplicates */
    private Set<Replacement> findSortedReplacements(
        String text,
        WikipediaLanguage lang,
        List<ReplacementFinder> finders
    ) {
        // TreeSet to distinct and sort
        Set<Replacement> all = new TreeSet<>();
        // For loop to mock easily with an iterator
        for (ReplacementFinder finder : finders) {
            all.addAll(finder.findList(text, lang));
        }
        return all;
    }

    /* Return a stream of sorted replacements */
    private Stream<Replacement> removeNestedReplacements(Set<Replacement> replacements) {
        // We need to filter the stream items against the stream itself so it is not a stateless predicate.
        // We assume all the replacements in the stream are distinct, in this case,
        // this means there are not two replacements with the same start and end,
        // so the contain function is strict.

        // Filter to return the replacements which are NOT strictly contained in any other
        return replacements.stream().filter(r -> replacements.stream().noneMatch(r2 -> r2.contains(r)));
    }

    private List<Replacement> removeImmutables(Stream<Replacement> replacements, String text, WikipediaLanguage lang) {
        // LinkedList to remove items. Order is kept.
        List<Replacement> replacementList = replacements.collect(Collectors.toCollection(LinkedList::new));

        // No need to find the immutables if there are no replacements
        if (replacementList.isEmpty()) {
            return Collections.emptyList();
        }

        for (Immutable immutable : immutableFindService.findImmutables(text, lang)) {
            // Detect empty immutables
            if (StringUtils.isBlank(immutable.getText())) {
                // As we don't have the page title we trace a snippet of text around the immutable
                LOGGER.warn(
                    "Empty immutable: {} - {} - {} - {}",
                    immutable.getFinder().getClass().getSimpleName(),
                    immutable.getStart(),
                    lang,
                    FinderUtils.getContextAroundWord(text, immutable.getStart(), immutable.getEnd(), 100)
                );
            }

            // Detect too long immutables likely to be errors in the text or in the finder
            if (showLongImmutables && immutable.getText().length() > immutable.getFinder().getMaxLength()) {
                LOGGER.warn(
                    "Too long immutable: {} - {} - {} - {}",
                    immutable.getFinder().getClass().getSimpleName(),
                    immutable.getText().length(),
                    lang,
                    immutable.getText()
                );
            }

            replacementList.removeIf(immutable::contains);

            // No need to continue finding the immutables if there are no replacements
            if (replacementList.isEmpty()) {
                return Collections.emptyList();
            }
        }

        return replacementList;
    }

    private List<Replacement> addContextToReplacements(List<Replacement> replacements, String text) {
        return replacements.stream().map(r -> addContextToReplacement(r, text)).collect(Collectors.toList());
    }

    private Replacement addContextToReplacement(Replacement replacement, String text) {
        return replacement.withContext(
            FinderUtils.getContextAroundWord(text, replacement.getStart(), replacement.getEnd(), CONTEXT_THRESHOLD)
        );
    }
}
