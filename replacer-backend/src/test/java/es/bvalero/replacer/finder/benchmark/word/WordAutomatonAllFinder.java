package es.bvalero.replacer.finder.benchmark.word;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;

class WordAutomatonAllFinder implements BenchmarkFinder {

    private final RunAutomaton wordAutomaton;
    private final Set<String> words;

    WordAutomatonAllFinder(Collection<String> words) {
        this.wordAutomaton = new RunAutomaton(new RegExp("<L>+").toAutomaton(new DatatypesAutomatonProvider()));
        this.words = new HashSet<>(words);
    }

    @Override
    public Iterable<BenchmarkResult> find(WikipediaPage page) {
        final String text = page.getContent();
        // Find all words in the text with an automaton and check if they are in the list
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final AutomatonMatcher m = this.wordAutomaton.newMatcher(text);
        while (m.find()) {
            final int start = m.start();
            final String word = m.group();
            if (this.words.contains(word) && FinderUtils.isWordCompleteInText(start, word, text)) {
                matches.add(BenchmarkResult.of(start, word));
            }
        }
        return matches;
    }
}
