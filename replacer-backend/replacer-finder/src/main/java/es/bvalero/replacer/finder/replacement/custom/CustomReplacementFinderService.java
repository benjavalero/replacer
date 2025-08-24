package es.bvalero.replacer.finder.replacement.custom;

import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.finder.replacement.ReplacementFinderAbstractService;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
class CustomReplacementFinderService
    extends ReplacementFinderAbstractService
    implements FinderService<Replacement>, CustomReplacementFindApi {

    public CustomReplacementFinderService(ImmutableFindApi immutableFindApi) {
        super(immutableFindApi);
    }

    @Override
    public SortedSet<Replacement> find(FinderPage page) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<Replacement> findStream(FinderPage page) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Finder<Replacement>> getFinders() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<Replacement> findCustomReplacements(
        FinderPage page,
        CustomReplacementFindRequest customReplacementFindRequest
    ) {
        // There will usually be much more immutables found than results.
        // Thus, it is better to obtain first all the results, and then obtain the immutables one by one,
        // aborting in case the replacement list gets empty. This way we can avoid lots of immutable calculations.
        final CustomReplacementFinder finder = CustomReplacementFinder.of(
            customReplacementFindRequest.getWord(),
            customReplacementFindRequest.isCaseSensitive(),
            customReplacementFindRequest.getComment()
        );
        Stream<Replacement> customResults = findStream(page, Collections.singleton(finder));
        SortedSet<Replacement> sortedResults = new TreeSet<>();
        customResults.forEach(sortedResults::add);
        return super.filterResults(page, sortedResults);
    }
}
