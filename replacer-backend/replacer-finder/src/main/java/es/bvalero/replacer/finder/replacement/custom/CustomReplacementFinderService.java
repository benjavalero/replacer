package es.bvalero.replacer.finder.replacement.custom;

import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;

@Service
public class CustomReplacementFinderService
    extends ReplacementFinderService
    implements FinderService<Replacement>, CustomReplacementFindService {

    public CustomReplacementFinderService(
        List<ReplacementFinder> replacementFinders,
        ImmutableFinderService immutableFinderService
    ) {
        super(replacementFinders, immutableFinderService);
    }

    @Override
    public Set<Replacement> find(FinderPage page) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Replacement> findIterable(FinderPage page) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Finder<Replacement>> getFinders() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Replacement> findCustomReplacements(FinderPage page, CustomMisspelling customMisspelling) {
        // There will usually be much more immutables found than results.
        // Thus, it is better to obtain first all the results, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        final CustomReplacementFinder finder = CustomReplacementFinder.of(customMisspelling);
        Iterable<Replacement> customResults = findIterable(page, Set.of(finder));
        Set<Replacement> sortedResults = new TreeSet<>();
        customResults.forEach(sortedResults::add);
        return filterResults(page, sortedResults);
    }
}
