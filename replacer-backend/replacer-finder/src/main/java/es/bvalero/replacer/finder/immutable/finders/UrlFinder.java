package es.bvalero.replacer.finder.immutable.finders;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find URLs, e.g. `https://www.google.es`
 */
@Component
class UrlFinder implements ImmutableFinder {

    // The automaton works quite well and provides support for complex URLs,
    // so it is worth to use it even if it doesn't give the best performance.
    private static final String REGEX_URL = "https?://<URI>";

    private static final RunAutomaton AUTOMATON_URL = new RunAutomaton(
        new RegExp(REGEX_URL).toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return AutomatonMatchFinder.find(page.getContent(), AUTOMATON_URL);
    }

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.MEDIUM;
    }
}
