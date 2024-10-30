package es.bvalero.replacer.finder.benchmark.surname;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class SurnameAutomatonAlternateCompleteFinder implements BenchmarkFinder {

    private final RunAutomaton words;

    SurnameAutomatonAlternateCompleteFinder(Collection<String> words) {
        final String alternations = "<Lu><L>+ (" + FinderUtils.joinAlternate(words) + ")";
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // Build an alternate automaton with all the complete words and match it against the text
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            final int pos = m.group().indexOf(' ') + 1;
            final int start = m.start() + pos;
            final String matchText = m.group().substring(pos);
            if (FinderUtils.isWordCompleteInText(start, matchText, text)) {
                matches.add(BenchmarkResult.of(start, matchText));
            }
        }
        return matches;
    }
}
