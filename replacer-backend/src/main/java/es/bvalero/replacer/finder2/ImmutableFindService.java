package es.bvalero.replacer.finder2;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Independent service that finds all the immutables in a given text.
 *
 * It is composed by several specific immutable finders: URL, quotes, etc.
 * which implement the same interface.
 *
 * The service applies all these specific finders one by one, and returns
 * an iterator with the results.
 */
@Service
public class ImmutableFindService {
    @Autowired
    private List<ImmutableFinder> immutableFinders;

    public Iterable<Immutable> findImmutables(String text) {
        ImmutableIterator iterator = new ImmutableIterator(text, immutableFinders);
        return () -> iterator;
    }
}
