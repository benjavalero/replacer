package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Independent service that finds all the replacements in a given text.
 * It is composed by several specific replacement finders: misspelling, date format, etc.
 * which implement the same interface.
 * The service applies all these specific finders and returns the collected results.
 */
@Slf4j
@Service
public class ReplacementFinderService extends ImmutableFilterFinderService<Replacement> {

    @Autowired
    private List<ReplacementFinder> replacementFinders;

    @Autowired
    private ImmutableFinderService immutableFinderService;

    @Override
    public List<Finder<Replacement>> getFinders() {
        return new ArrayList<>(replacementFinders);
    }
}
