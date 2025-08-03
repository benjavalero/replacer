package es.bvalero.replacer.finder;

import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface ImmutableFindApi {
    /** Find all immutables in the page content */
    Iterable<Immutable> findImmutables(FinderPage page);
}
