package es.bvalero.replacer.finder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Interface to be implemented by any class returning a collection of cosmetics.
 */
public interface CosmeticFinder {
    Iterable<Cosmetic> find(String text);

    default List<Cosmetic> findList(String text) {
        return StreamSupport.stream(find(text).spliterator(), false).collect(Collectors.toList());
    }
}
