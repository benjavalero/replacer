package es.bvalero.replacer.finder.benchmark.surname;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

class SurnameAutomatonAlternateCompleteFinder implements BenchmarkFinder {

    private final RunAutomaton words;

    SurnameAutomatonAlternateCompleteFinder(Collection<String> words) {
        final String alternations = "<Lu><L>+ (" + StringUtils.join(words, "|") + ")";
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        // Build an alternate automaton with all the complete words and match it against the text
        final Set<BenchmarkResult> matches = new HashSet<>();
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
