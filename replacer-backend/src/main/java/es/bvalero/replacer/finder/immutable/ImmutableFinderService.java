package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.common.domain.Immutable;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImmutableFinderService implements FinderService<Immutable> {

    @Autowired
    private List<ImmutableFinder> immutableFinders;

    @PostConstruct
    public void sortImmutableFinders() {
        Collections.sort(immutableFinders);
    }

    @Override
    public Iterable<Finder<Immutable>> getFinders() {
        return new ArrayList<>(immutableFinders);
    }
}
