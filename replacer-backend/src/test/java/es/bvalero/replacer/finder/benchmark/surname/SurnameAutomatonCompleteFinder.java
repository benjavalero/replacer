package es.bvalero.replacer.finder.benchmark.surname;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;

import java.util.*;

class SurnameAutomatonCompleteFinder implements BenchmarkFinder {
    private final List<RunAutomaton> words;

    SurnameAutomatonCompleteFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(new RunAutomaton(new RegExp("<Lu><L>+ " + word).toAutomaton(new DatatypesAutomatonProvider())));
        }
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        // We loop over all the words and find them completely in the text with an automaton
        Set<FinderResult> matches = new HashSet<>();
        for (RunAutomaton word : this.words) {
            AutomatonMatcher m = word.newMatcher(text);
            while (m.find()) {
                int pos = m.group().indexOf(' ') + 1;
                matches.add(FinderResult.of(m.start() + pos, m.group().substring(pos)));
            }
        }
        return matches;
    }
}
