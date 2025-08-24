package es.bvalero.replacer.finder;

import java.util.SortedSet;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface CustomReplacementFindApi {
    /**
     * Find all custom replacements in the page content ignoring the ones contained in immutables
     */
    SortedSet<Replacement> findCustomReplacements(
        FinderPage page,
        CustomReplacementFindRequest customReplacementFindRequest
    );
}
