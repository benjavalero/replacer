package es.bvalero.replacer.finder.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.common.Finder;
import es.bvalero.replacer.finder.common.FinderService;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import es.bvalero.replacer.page.IndexablePage;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Independent service that finds all the replacements in a given text.
 * It is composed by several specific replacement finders: misspelling, date format, etc.
 * which implement the same interface.
 * The service applies all these specific finders and returns the collected results.
 */
@Slf4j
@Service
public class ReplacementFinderService implements FinderService<Replacement> {

    @Autowired
    private List<ReplacementFinder> replacementFinders;

    @Autowired
    private ImmutableFinderService immutableFinderService;

    @Autowired
    private MisspellingSimpleFinder misspellingSimpleFinder;

    @Autowired
    private MisspellingComposedFinder misspellingComposedFinder;

    @Loggable(prepend = true, value = Loggable.TRACE, unit = TimeUnit.SECONDS)
    @Override
    public Iterable<Replacement> find(IndexablePage page) {
        // The replacement finder ignores in the response all the found replacements which are contained
        // in the found immutables. Usually there will be much more immutables found than replacements.
        // Thus it is better to obtain first all the replacements, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        return findFilteredReplacements(page, replacementFinders);
    }

    // TODO: Split the steps so this service retrieves all the replacements in a page
    // but we can inherit it to make services which retrieve a specific custom replacement
    // or only the ones of a given type/subtype

    @Override
    public List<Finder<Replacement>> getFinders() {
        return new ArrayList<>(replacementFinders);
    }

    @Loggable(prepend = true, value = Loggable.TRACE, unit = TimeUnit.SECONDS)
    public List<Replacement> findCustomReplacements(IndexablePage page, String replacement, String suggestion) {
        CustomReplacementFinder finder = new CustomReplacementFinder(replacement, suggestion);
        return findFilteredReplacements(page, Collections.singletonList(finder));
    }

    /* Find all the replacements in the text but return only the necessary */
    private List<Replacement> findFilteredReplacements(IndexablePage page, List<ReplacementFinder> finders) {
        Set<Replacement> all = findSortedReplacements(page, finders);

        // Different finders could "collide" in some results so we discard the ones contained in others
        Stream<Replacement> notNested = removeNestedReplacements(all);

        // Ignore the replacements contained in immutables
        return removeImmutables(notNested, page);
    }

    /* Find all the replacements in the text sorted and without duplicates */
    private Set<Replacement> findSortedReplacements(IndexablePage page, List<ReplacementFinder> finders) {
        // TreeSet to distinct and sort
        Set<Replacement> all = new TreeSet<>();
        // For loop to mock easily with an iterator
        for (ReplacementFinder finder : finders) {
            all.addAll(IterableUtils.toList(finder.find(page)));
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

    private List<Replacement> removeImmutables(Stream<Replacement> replacements, IndexablePage page) {
        // LinkedList to remove items. Order is kept.
        List<Replacement> replacementList = replacements.collect(Collectors.toCollection(LinkedList::new));

        // No need to find the immutables if there are no replacements
        if (replacementList.isEmpty()) {
            return Collections.emptyList();
        }

        for (Immutable immutable : immutableFinderService.find(page)) {
            replacementList.removeIf(r -> this.containsOrIntersects(immutable, r));

            // No need to continue finding the immutables if there are no replacements
            if (replacementList.isEmpty()) {
                return Collections.emptyList();
            }
        }

        return replacementList;
    }

    private boolean containsOrIntersects(Immutable immutable, Replacement r) {
        // For not-composed misspellings we just check a simple contains
        return ReplacementType.MISSPELLING_COMPOSED.equals(r.getType())
            ? immutable.intersects(r)
            : immutable.contains(r);
    }

    /** Checks if the given word exists as a misspelling and in this case returns the type */
    public Optional<String> findExistingMisspelling(String word, WikipediaLanguage lang) {
        if (misspellingSimpleFinder.findMisspellingByWord(word, lang).isPresent()) {
            return Optional.of(misspellingSimpleFinder.getType());
        } else if (misspellingComposedFinder.findMisspellingByWord(word, lang).isPresent()) {
            return Optional.of(misspellingComposedFinder.getType());
        } else {
            return Optional.empty();
        }
    }

    @TestOnly
    public static Set<String> getMisspellings() throws ReplacerException {
        String text = WikipediaUtils.getFileContent("/offline/misspelling-list.txt");
        MisspellingManager misspellingManager = new MisspellingManager();
        Set<Misspelling> misspellings = misspellingManager.parseItemsText(text);
        MisspellingFinder misspellingFinder = new MisspellingSimpleFinder();
        return misspellingFinder.buildMisspellingMap(misspellings).keySet();
    }

    @TestOnly
    public static Set<String> getComposedMisspellings() throws ReplacerException {
        String text = WikipediaUtils.getFileContent("/offline/composed-misspellings.txt");
        MisspellingManager misspellingManager = new MisspellingComposedManager();
        Set<Misspelling> misspellings = misspellingManager.parseItemsText(text);
        MisspellingFinder misspellingFinder = new MisspellingComposedFinder();
        return misspellingFinder.buildMisspellingMap(misspellings).keySet();
    }
}
