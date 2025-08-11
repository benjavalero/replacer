package es.bvalero.replacer.finder;

import java.util.Collection;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface CosmeticApi {
    /** Find all cosmetics in the page content */
    Collection<Cosmetic> findCosmetics(FinderPage page);

    /** Return the page with new content after applying all the cosmetic changes */
    FinderPage applyCosmeticChanges(FinderPage page);
}
