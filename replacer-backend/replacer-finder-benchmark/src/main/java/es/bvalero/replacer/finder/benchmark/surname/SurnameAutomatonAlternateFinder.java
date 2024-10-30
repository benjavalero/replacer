package es.bvalero.replacer.finder.benchmark.surname;

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

class SurnameAutomatonAlternateFinder implements BenchmarkFinder {

    private final RunAutomaton words;

    SurnameAutomatonAlternateFinder(Collection<String> words) {
        final String alternations = '(' + FinderUtils.joinAlternate(words) + ')';
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // Build an alternate automaton with all the words and match it against the text
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            if (
                FinderUtils.isWordCompleteInText(m.start(), m.group(), text) &&
                FinderUtils.isWordPrecededByUpperCase(m.start(), text)
            ) {
                matches.add(BenchmarkResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }
}
