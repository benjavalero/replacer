package es.bvalero.replacer.finder.benchmark.uppercase;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.*;

class UppercaseAutomatonIterateFinder implements BenchmarkFinder {

    private final List<RunAutomaton> words;

    UppercaseAutomatonIterateFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(
                    new RunAutomaton(new RegExp("[!#*|=.]<Zs>*" + word).toAutomaton(new DatatypesAutomatonProvider()))
                );
        }
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        // We loop over all the words and find them in the text with an automaton
        Set<FinderResult> matches = new HashSet<>();
        for (RunAutomaton word : this.words) {
            AutomatonMatcher m = word.newMatcher(text);
            while (m.find()) {
                String w = m.group().substring(1).trim();
                int pos = m.group().indexOf(w);
                matches.add(FinderResult.of(m.start() + pos, w));
            }
        }
        return matches;
    }
}
