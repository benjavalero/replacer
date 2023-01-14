package es.bvalero.replacer.finder.benchmark.surname;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.BenchmarkResult;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class SurnameAutomatonFinder implements BenchmarkFinder {

    private final Collection<RunAutomaton> words = new ArrayList<>();

    SurnameAutomatonFinder(Collection<String> words) {
        for (String word : words) {
            this.words.add(new RunAutomaton(new RegExp(word).toAutomaton()));
        }
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // We loop over all the words and find them in the text with an automaton
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        for (RunAutomaton word : this.words) {
            final AutomatonMatcher m = word.newMatcher(text);
            while (m.find()) {
                if (FinderUtils.isWordPrecededByUpperCase(m.start(), text)) {
                    matches.add(BenchmarkResult.of(m.start(), m.group()));
                }
            }
        }
        return matches;
    }
}
