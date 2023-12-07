package es.bvalero.replacer.page.save;

import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.FinderPage;
import java.util.Collection;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface CosmeticFindService {
    /** Find all cosmetics in the page content */
    Collection<Cosmetic> findCosmetics(FinderPage page);
}
