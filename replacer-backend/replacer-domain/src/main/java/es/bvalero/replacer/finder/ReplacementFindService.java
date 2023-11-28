package es.bvalero.replacer.finder;

import java.util.Collection;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface ReplacementFindService {
    /** Find all replacements in the page content ignoring the ones contained in immutables */
    Collection<Replacement> findReplacements(FinderPage page);
}
