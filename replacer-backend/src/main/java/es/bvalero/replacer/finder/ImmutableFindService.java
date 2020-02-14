package es.bvalero.replacer.finder;

import java.util.List;
import java.util.stream.Collectors;
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

    Iterable<Immutable> findImmutables(String text) {
        // Collect to a list which is already an iterable
        return new IterableOfIterable<>(
            immutableFinders.stream().map(finder -> finder.find(text)).collect(Collectors.toList())
        );
    }
}
