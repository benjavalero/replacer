package es.bvalero.replacer.finder.benchmark.uppercase;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.BenchmarkResult;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;

class UppercaseAutomatonIterateFinder extends UppercaseBenchmarkFinder {

    private final List<RunAutomaton> words;

    UppercaseAutomatonIterateFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            String regex = String.format("(%s)<Zs>*%s", FinderUtils.joinAlternate(PUNCTUATIONS), word);
            this.words.add(new RunAutomaton(new RegExp(regex).toAutomaton(new DatatypesAutomatonProvider())));
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
                String w = m.group().substring(1).trim();
                int pos = m.group().indexOf(w);
                matches.add(BenchmarkResult.of(m.start() + pos, w));
            }
        }
        return matches;
    }
}
