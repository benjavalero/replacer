package es.bvalero.replacer.finder.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.finder.common.Finder;
import es.bvalero.replacer.finder.common.FinderPage;
import es.bvalero.replacer.finder.common.FinderService;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
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

    @Loggable(prepend = true, value = Loggable.TRACE, unit = TimeUnit.SECONDS)
    @Override
    public Iterable<Replacement> find(FinderPage page) {
        // The replacement finder ignores in the response all the found replacements which are contained
        // in the found immutables. Usually there will be much more immutables found than replacements.
        // Thus it is better to obtain first all the replacements, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        return findReplacements(page, getFinders());
    }

    protected Iterable<Replacement> findReplacements(FinderPage page, List<Finder<Replacement>> finders) {
        // First we retrieve all replacements and later filter only the valid
        Iterable<Replacement> allReplacements = find(page, finders);
        return filterReplacements(page, allReplacements);
    }

    @Override
    public List<Finder<Replacement>> getFinders() {
        return new ArrayList<>(replacementFinders);
    }

    private Iterable<Replacement> filterReplacements(FinderPage page, Iterable<Replacement> allReplacements) {
        // Remove duplicates. By the way we sort the collection.
        Iterable<Replacement> noDupes = removeDuplicates(allReplacements);

        // Remove nested. There might be replacements (strictly) contained in others.
        Iterable<Replacement> noNested = removeNested(noDupes);

        // Remove the ones contained in immutables
        return removeImmutables(page, noNested);
    }

    private Iterable<Replacement> removeDuplicates(Iterable<Replacement> replacements) {
        // TreeSet to distinct and sort
        Set<Replacement> noDupes = new TreeSet<>();
        for (Replacement replacement : replacements) {
            noDupes.add(replacement);
        }
        return noDupes;
    }

    private Iterable<Replacement> removeNested(Iterable<Replacement> replacements) {
        // We need to filter the items against the collection itself so it is not a stateless predicate.
        // We assume all the replacements in the iterable are distinct, in this case,
        // this means there are not two replacements with the same start and end,
        // so the contain function is strict.

        // Filter to return the replacements which are NOT strictly contained in any other
        return toStream(replacements)
            .filter(r -> toStream(replacements).noneMatch(r2 -> r2.contains(r)))
            .collect(Collectors.toList());
    }

    private Stream<Replacement> toStream(Iterable<Replacement> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    private Iterable<Replacement> removeImmutables(FinderPage page, Iterable<Replacement> replacements) {
        // LinkedList to remove items. Order is kept.
        List<Replacement> replacementList = new LinkedList<>(IterableUtils.toList(replacements));

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
