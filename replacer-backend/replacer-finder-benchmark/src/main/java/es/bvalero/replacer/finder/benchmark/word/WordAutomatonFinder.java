package es.bvalero.replacer.finder.benchmark.word;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Loop over all the words/expressions and find them in the text with a regex.
 * Then the result is checked to be complete in the text.
 * There is no "complete" version as the automaton doesn't allow complex regex.
 */
class WordAutomatonFinder implements BenchmarkFinder {

    private final List<RunAutomaton> automata;

    WordAutomatonFinder(Collection<String> words) {
        this.automata = new ArrayList<>();
        for (String word : words) {
            this.automata.add(new RunAutomaton(new RegExp(cleanWord(word)).toAutomaton()));
        }
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        for (RunAutomaton automaton : this.automata) {
            final AutomatonMatcher m = automaton.newMatcher(text);
            while (m.find()) {
                final int start = m.start();
                final String word = m.group();
                if (FinderUtils.isWordCompleteInText(start, word, text)) {
                    matches.add(BenchmarkResult.of(start, word));
                }
            }
        }
        return matches.stream();
    }
}
