package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.finder.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
class CosmeticFinderService implements FinderService<Cosmetic>, CosmeticFindService {

    // Dependency injection
    private final List<CosmeticFinder> cosmeticFinders;

    public CosmeticFinderService(List<CosmeticFinder> cosmeticFinders) {
        this.cosmeticFinders = cosmeticFinders;
    }

    @PostConstruct
    public void sortImmutableFinders() {
        Collections.sort(cosmeticFinders);
    }

    @Override
    public Iterable<Finder<Cosmetic>> getFinders() {
        return new ArrayList<>(cosmeticFinders);
    }

    @Override
    public Collection<Cosmetic> findCosmetics(FinderPage page) {
        return this.find(page);
    }
}
