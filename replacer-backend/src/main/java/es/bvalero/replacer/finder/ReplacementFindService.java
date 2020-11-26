package es.bvalero.replacer.finder;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
        return findAllReplacements(text, lang, replacementFinders);
    }

    @Loggable(prepend = true, value = Loggable.TRACE, unit = TimeUnit.SECONDS)
    public List<Replacement> findCustomReplacements(
        String text,
        String replacement,
        String suggestion,
        WikipediaLanguage lang
    ) {
        CustomReplacementFinder finder = new CustomReplacementFinder(replacement, suggestion);
        return findAllReplacements(text, lang, Collections.singletonList(finder));
    }

    private List<Replacement> findAllReplacements(
        String text,
        WikipediaLanguage lang,
        List<ReplacementFinder> finders
    ) {
        Stream<Replacement> all = finders.stream().flatMap(finder -> finder.findStream(text, lang)).sorted();

        Stream<Replacement> distinct = removeNestedReplacements(all);

        // Ignore the replacements contained in immutables
        List<Replacement> noIgnored = removeImmutables(distinct, text, lang);

        return addContextToReplacements(noIgnored, text);
    }

    private Stream<Replacement> removeNestedReplacements(Stream<Replacement> replacements) {
        // We need to filter the stream items against the stream itself so it is not a stateless predicate
        // We assume all the replacements in the stream are distinct, in this case,
        // this means there are not two replacements with the same start and end,
        // so the contain function is strict.
        List<Replacement> distinctList = replacements.distinct().collect(Collectors.toList());

        // Filter to return the replacements which are NOT strictly contained in any other
        return distinctList.stream().filter(r -> distinctList.stream().noneMatch(r2 -> r2.contains(r)));
    }

    private List<Replacement> removeImmutables(Stream<Replacement> replacements, String text, WikipediaLanguage lang) {
        // Build a list from the stream and remove the items contained in any immutable
        List<Replacement> replacementList = replacements.collect(Collectors.toCollection(LinkedList::new));

        // No need to find the immutables if there are no replacements
        if (replacementList.isEmpty()) {
            return Collections.emptyList();
        }

        for (Immutable immutable : immutableFindService.findImmutables(text, lang)) {
            // Detect empty immutables
            if (StringUtils.isBlank(immutable.getText())) {
                LOGGER.warn("Empty immutable: {} - {}", immutable, text);
            }

            // Detect too long immutables likely to be errors in the text or in the finder
            if (showLongImmutables && immutable.getText().length() > immutable.getFinder().getMaxLength()) {
                LOGGER.warn(
                    "Immutable too long: {}\t{}\t{}",
                    immutable.getFinder().getClass().getSimpleName(),
                    immutable.getText().length(),
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
        int limitLeft = Math.max(0, replacement.getStart() - CONTEXT_THRESHOLD);
        int limitRight = Math.min(text.length() - 1, replacement.getEnd() + CONTEXT_THRESHOLD);
        String context = text.substring(limitLeft, limitRight);
        return replacement.withContext(context);
    }
}
