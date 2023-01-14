package es.bvalero.replacer.finder;

import java.util.Collection;

public interface CosmeticFindService {
    /** Find all cosmetics in the page content */
    Collection<Cosmetic> findCosmetics(FinderPage page);
}
