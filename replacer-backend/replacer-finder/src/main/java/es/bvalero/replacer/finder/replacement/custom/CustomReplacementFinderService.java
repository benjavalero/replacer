package es.bvalero.replacer.finder.replacement.custom;

import es.bvalero.replacer.finder.*;
import java.util.Collections;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CustomReplacementFinderService implements FinderService<Replacement> {

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

    public Iterable<Replacement> findCustomReplacements(FinderPage page, CustomOptions customOptions) {
        final CustomReplacementFinder finder = CustomReplacementFinder.of(customOptions);
        return findIterable(page, Collections.singleton(finder));
    }
}
