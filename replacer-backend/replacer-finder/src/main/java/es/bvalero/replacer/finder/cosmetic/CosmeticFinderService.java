package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CosmeticFinderService implements FinderService<Cosmetic> {

    @Autowired
    private List<CosmeticFinder> cosmeticFinders;

    @Override
    public Iterable<Finder<Cosmetic>> getFinders() {
        return new ArrayList<>(cosmeticFinders);
    }
}
