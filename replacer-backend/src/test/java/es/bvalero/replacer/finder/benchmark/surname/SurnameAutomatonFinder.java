package es.bvalero.replacer.finder.benchmark.surname;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class SurnameAutomatonFinder implements BenchmarkFinder {

    private final Collection<RunAutomaton> words = new ArrayList<>();

    SurnameAutomatonFinder(Collection<String> words) {
        for (String word : words) {
            this.words.add(new RunAutomaton(new RegExp(word).toAutomaton()));
        }
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        // We loop over all the words and find them in the text with an automaton
        Set<BenchmarkResult> matches = new HashSet<>();
        for (RunAutomaton word : this.words) {
            final AutomatonMatcher m = word.newMatcher(text);
            while (m.find()) {
                if (FinderUtils.isWordPrecededByUppercase(m.start(), m.group(), text)) {
                    matches.add(BenchmarkResult.of(m.start(), m.group()));
                }
            }
        }
        return matches;
    }
}
