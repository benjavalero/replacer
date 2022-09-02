package es.bvalero.replacer.finder.benchmark.simple;

import static org.apache.commons.lang3.StringUtils.SPACE;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import java.util.regex.MatchResult;

class SimpleAutomatonFinder implements BenchmarkFinder {

    private static final String SIMPLE_REGEX = SPACE;
    private static final RunAutomaton SIMPLE_AUTOMATON = new RunAutomaton(new RegExp(SIMPLE_REGEX).toAutomaton());

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return AutomatonMatchFinder.find(page.getContent(), SIMPLE_AUTOMATON);
    }
}
