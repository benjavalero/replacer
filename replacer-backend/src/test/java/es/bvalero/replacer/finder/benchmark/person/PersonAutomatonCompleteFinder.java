package es.bvalero.replacer.finder.benchmark.person;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
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
    public Set<FinderResult> findMatches(String text) {
        // We loop over all the words and find them completely in the text with an automaton
        Set<FinderResult> matches = new HashSet<>();
        for (RunAutomaton word : this.words) {
            AutomatonMatcher m = word.newMatcher(text);
            while (m.find()) {
                matches.add(FinderResult.of(m.start(), m.group().substring(0, m.group().length() - 2)));
            }
        }
        return matches;
    }
}
