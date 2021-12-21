package es.bvalero.replacer.finder.replacement.custom;

import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderService;
import es.bvalero.replacer.finder.replacement.Replacement;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CustomReplacementFinderService implements FinderService<Replacement> {

    @Override
    public List<Replacement> find(FinderPage page) {
        throw new IllegalCallerException();
    }

    @Override
    public Iterable<Replacement> findIterable(FinderPage page) {
        throw new IllegalCallerException();
    }

    @Override
    public Iterable<Finder<Replacement>> getFinders() {
        throw new IllegalCallerException();
    }

    public Iterable<Replacement> findCustomReplacements(FinderPage page, CustomOptions customOptions) {
        final CustomReplacementFinder finder = CustomReplacementFinder.of(customOptions);
        return findIterable(page, Collections.singleton(finder));
    }
}
