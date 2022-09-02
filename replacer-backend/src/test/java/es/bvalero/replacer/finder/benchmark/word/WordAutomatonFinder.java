package es.bvalero.replacer.finder.benchmark.word;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;

class WordAutomatonFinder implements BenchmarkFinder {

    private final List<RunAutomaton> automata;

    WordAutomatonFinder(Collection<String> words) {
        this.automata = new ArrayList<>();
        for (String word : words) {
            this.automata.add(new RunAutomaton(new RegExp(word).toAutomaton()));
        }
    }

    @Override
    public Set<BenchmarkResult> find(WikipediaPage page) {
        String text = page.getContent();
        // We loop over all the words and find them in the text with an automaton
        Set<BenchmarkResult> matches = new HashSet<>();
        for (RunAutomaton automaton : this.automata) {
            AutomatonMatcher m = automaton.newMatcher(text);
            while (m.find()) {
                if (FinderUtils.isWordCompleteInText(m.start(), m.group(), text)) {
                    matches.add(BenchmarkResult.of(m.start(), m.group()));
                }
            }
        }
        return matches;
    }
}
