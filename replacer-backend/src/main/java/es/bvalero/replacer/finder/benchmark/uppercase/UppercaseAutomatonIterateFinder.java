package es.bvalero.replacer.finder.benchmark.uppercase;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.*;

class UppercaseAutomatonIterateFinder implements BenchmarkFinder {
    private final Map<String, RunAutomaton> words;

    UppercaseAutomatonIterateFinder(Collection<String> words) {
        this.words = new HashMap<>();
        for (String word : words) {
            this.words.put(
                    word,
                    new RunAutomaton(new RegExp("[!#*|=.]<Z>*" + word).toAutomaton(new DatatypesAutomatonProvider()))
                );
        }
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        // We loop over all the words and find them in the text with an automaton
        Set<FinderResult> matches = new HashSet<>();
        for (Map.Entry<String, RunAutomaton> word : this.words.entrySet()) {
            AutomatonMatcher m = word.getValue().newMatcher(text);
            while (m.find()) {
                int pos = m.group().indexOf(word.getKey());
                matches.add(FinderResult.of(m.start() + pos, word.getKey()));
            }
        }
        return matches;
    }
}
