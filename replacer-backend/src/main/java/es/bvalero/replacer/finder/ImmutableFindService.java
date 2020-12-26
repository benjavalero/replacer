package es.bvalero.replacer.finder;

import es.bvalero.replacer.page.IndexablePage;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Independent service that finds all the immutables in a given text.
 *
 * It is composed by several specific immutable finders: URL, quotes, etc. which
 * implement the same interface.
 *
 * The service applies all these specific finders one by one, and returns an
 * iterator with the results.
 */
@Service
class ImmutableFindService {

    @Autowired
    private List<ImmutableFinder> immutableFinders;

    @PostConstruct
    public void sortImmutableFinders() {
        Collections.sort(immutableFinders);
    }

    Iterable<Immutable> findImmutables(IndexablePage page) {
        // Collect to a list which is already an iterable
        // Sort the finders by priority
        return new IterableOfIterable<>(
            immutableFinders.stream().map(finder -> finder.find(page)).collect(Collectors.toList())
        );
    }
}
