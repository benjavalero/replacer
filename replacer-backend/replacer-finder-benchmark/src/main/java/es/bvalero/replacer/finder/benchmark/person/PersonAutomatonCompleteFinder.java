package es.bvalero.replacer.finder.benchmark.person;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.*;

class PersonAutomatonCompleteFinder implements BenchmarkFinder {

    private final List<RunAutomaton> words;

    PersonAutomatonCompleteFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(
                    new RunAutomaton(new RegExp(word + "<Z><Lu>").toAutomaton(new DatatypesAutomatonProvider()))
                );
        }
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // We loop over all the words and find them completely in the text with an automaton
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        for (RunAutomaton word : this.words) {
            final AutomatonMatcher m = word.newMatcher(text);
            while (m.find()) {
                matches.add(BenchmarkResult.of(m.start(), m.group().substring(0, m.group().length() - 2)));
            }
        }
        return matches;
    }
}
