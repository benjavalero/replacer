package es.bvalero.replacer.finder.benchmark.person;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class PersonAutomatonAlternateCompleteFinder implements BenchmarkFinder {

    private final RunAutomaton words;

    PersonAutomatonAlternateCompleteFinder(Collection<String> words) {
        String alternations = "(" + FinderUtils.joinAlternate(words) + ")<Z><Lu>";
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // Build an alternate automaton with all the complete words and match it against the text
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            matches.add(BenchmarkResult.of(m.start(), m.group().substring(0, m.group().length() - 2)));
        }
        return matches;
    }
}
