package es.bvalero.replacer.finder.benchmark.uppercase;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
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
    public Set<BenchmarkResult> find(WikipediaPage page) {
        String text = page.getContent();
        // We loop over all the words and find them in the text with an automaton
        Set<BenchmarkResult> matches = new HashSet<>();
        for (RunAutomaton word : this.words) {
            AutomatonMatcher m = word.newMatcher(text);
            while (m.find()) {
                String w = m.group().substring(1).trim();
                int pos = m.group().indexOf(w);
                matches.add(BenchmarkResult.of(m.start() + pos, w));
            }
        }
        return matches;
    }
}
