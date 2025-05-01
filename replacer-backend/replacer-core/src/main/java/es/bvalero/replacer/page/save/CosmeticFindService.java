package es.bvalero.replacer.page.save;

import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.FinderPage;
import java.util.Collection;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

// TODO: This is only used when saving but should be placed in the same package of replacement finders
// as a cosmetic is just a particular case of replacement

@SecondaryPort
public interface CosmeticFindService {
    /** Find all cosmetics in the page content */
    Collection<Cosmetic> findCosmetics(FinderPage page);
}
