package es.bvalero.replacer.finder;

import java.util.stream.Stream;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface ImmutableFindApi {
    /** Find all immutables in the page content */
    Stream<Immutable> findImmutables(FinderPage page);
}
