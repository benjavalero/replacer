package es.bvalero.replacer.finder.benchmark.word;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class WordAutomatonFinder implements BenchmarkFinder {

    private final List<RunAutomaton> automata;

    WordAutomatonFinder(Collection<String> words) {
        this.automata = new ArrayList<>();
        for (String word : words) {
            this.automata.add(new RunAutomaton(new RegExp(word).toAutomaton()));
        }
    }

    @Override
    public Iterable<BenchmarkResult> find(WikipediaPage page) {
        final String text = page.getContent();
        // We loop over all the words and find them in the text with a regex
        // We cannot use RegexMatchFinder in a loop
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
        return matches;
    }
}
