package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderService;
import es.bvalero.replacer.finder.Immutable;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ImmutableFinderService implements FinderService<Immutable> {

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
}
