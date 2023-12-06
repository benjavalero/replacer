package es.bvalero.replacer.finder;

import java.util.Collection;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface CustomReplacementFindService {
    /** Find all custom replacements in the page content ignoring the ones contained in immutables */
    Collection<Replacement> findCustomReplacements(
        FinderPage page,
        CustomReplacementFindRequest customReplacementFindRequest
    );
}
