package es.bvalero.replacer.finder.benchmark.surname;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class SurnameAutomatonCompleteFinder implements BenchmarkFinder {

    private final Collection<RunAutomaton> words = new ArrayList<>();

    SurnameAutomatonCompleteFinder(Collection<String> words) {
        for (String word : words) {
            this.words.add(
                    new RunAutomaton(new RegExp("<Lu><L>+ " + word).toAutomaton(new DatatypesAutomatonProvider()))
                );
        }
    }

    @Override
    public Set<BenchmarkResult> find(WikipediaPage page) {
        String text = page.getContent();
        // We loop over all the words and find them completely in the text with an automaton
        final Set<BenchmarkResult> matches = new HashSet<>();
        for (RunAutomaton word : this.words) {
            final AutomatonMatcher m = word.newMatcher(text);
            while (m.find()) {
                final int pos = m.group().indexOf(' ') + 1;
                final int start = m.start() + pos;
                final String matchText = m.group().substring(pos);
                if (FinderUtils.isWordCompleteInText(start, matchText, text)) {
                    matches.add(BenchmarkResult.of(start, matchText));
                }
            }
        }
        return matches;
    }
}
