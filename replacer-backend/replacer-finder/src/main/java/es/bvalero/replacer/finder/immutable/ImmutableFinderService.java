package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.*;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
class ImmutableFinderService implements FinderService<Immutable>, ImmutableFindApi {

    // Dependency injection
    private final List<ImmutableFinder> immutableFinders;

    public ImmutableFinderService(List<ImmutableFinder> immutableFinders) {
        this.immutableFinders = immutableFinders;
    }

    @PostConstruct
    public void sortImmutableFinders() {
        Collections.sort(immutableFinders);
    }

    @Override
    public Iterable<Finder<Immutable>> getFinders() {
        return new ArrayList<>(immutableFinders);
    }

    @Override
    public Iterable<Immutable> findImmutables(FinderPage page) {
        return this.findIterable(page);
    }
}
