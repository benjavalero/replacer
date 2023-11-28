package es.bvalero.replacer.finder;

import java.util.Collection;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface CosmeticFindService {
    /** Find all cosmetics in the page content */
    Collection<Cosmetic> findCosmetics(FinderPage page);
}
