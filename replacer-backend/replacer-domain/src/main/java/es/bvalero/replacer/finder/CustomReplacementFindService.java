package es.bvalero.replacer.finder;

import java.util.Collection;

public interface CustomReplacementFindService {
    /** Find all custom replacements in the page content ignoring the ones contained in immutables */
    Collection<Replacement> findCustomReplacements(FinderPage page, CustomOptions options);
}
