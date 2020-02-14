package es.bvalero.replacer.finder;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.collections4.IterableUtils;

/**
 * Interface to be implemented by any class returning a collection of cosmetics.
 */
public interface CosmeticFinder {
    Iterable<Cosmetic> find(String text);

    default Stream<Cosmetic> findStream(String text) {
        return StreamSupport.stream(find(text).spliterator(), false);
    }

    default List<Cosmetic> findList(String text) {
        return IterableUtils.toList(find(text));
    }
}
