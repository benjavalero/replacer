package es.bvalero.replacer.finder.benchmark.word;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class WordAutomatonAllFinder implements BenchmarkFinder {
    private final RunAutomaton wordPattern;
    private final Set<String> words;

    WordAutomatonAllFinder(Collection<String> words) {
        this.wordPattern = new RunAutomaton(new RegExp("(<L>|[-'])+").toAutomaton(new DatatypesAutomatonProvider()));
        this.words = new HashSet<>(words);
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        // Find all words in the text with an automaton and check if they are in the list
        Set<FinderResult> matches = new HashSet<>();
        AutomatonMatcher m = this.wordPattern.newMatcher(text);
        while (m.find()) {
            if (this.words.contains(m.group())) {
                matches.add(FinderResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }
}
