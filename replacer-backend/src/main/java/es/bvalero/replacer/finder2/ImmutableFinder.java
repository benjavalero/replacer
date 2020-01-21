package es.bvalero.replacer.finder2;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Interface to be implemented by any class returning a collection of immutables.
 *
 * For performance reasons, it is preferred to return them as an interator.
 */
public interface ImmutableFinder {
    Iterator<Immutable> findImmutables(String text);

    default List<Immutable> findImmutableList(String text) {
        Iterable<Immutable> iterable = () -> findImmutables(text);
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }
}
