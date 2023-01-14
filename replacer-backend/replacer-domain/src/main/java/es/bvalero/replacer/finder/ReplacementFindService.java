package es.bvalero.replacer.finder;

import java.util.Collection;

public interface ReplacementFindService {
    /** Find all replacements in the page content ignoring the ones contained in immutables */
    Collection<Replacement> findReplacements(FinderPage page);
}
