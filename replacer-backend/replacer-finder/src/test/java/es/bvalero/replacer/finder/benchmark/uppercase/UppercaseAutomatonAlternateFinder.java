package es.bvalero.replacer.finder.benchmark.uppercase;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.BenchmarkResult;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class UppercaseAutomatonAlternateFinder extends UppercaseBenchmarkFinder {

    private final RunAutomaton words;

    UppercaseAutomatonAlternateFinder(Collection<String> words) {
        String alternations = String.format(
            "(%s)<Zs>*(%s)",
            FinderUtils.joinAlternate(PUNCTUATIONS),
            FinderUtils.joinAlternate(words)
        );
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // Build an alternate automaton with all the words and match it against the text
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            String word = m.group().substring(1).trim();
            int pos = m.group().indexOf(word);
            matches.add(BenchmarkResult.of(m.start() + pos, word));
        }
        return matches;
    }
}
