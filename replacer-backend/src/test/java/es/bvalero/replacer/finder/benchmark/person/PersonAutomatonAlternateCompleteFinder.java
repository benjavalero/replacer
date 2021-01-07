package es.bvalero.replacer.finder.benchmark.person;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

class PersonAutomatonAlternateCompleteFinder implements BenchmarkFinder {

    private final RunAutomaton words;

    PersonAutomatonAlternateCompleteFinder(Collection<String> words) {
        String alternations = "(" + StringUtils.join(words, "|") + ")<Z><Lu>";
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        // Build an alternate automaton with all the complete words and match it against the text
        Set<FinderResult> matches = new HashSet<>();
        AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            matches.add(FinderResult.of(m.start(), m.group().substring(0, m.group().length() - 2)));
        }
        return matches;
    }
}
