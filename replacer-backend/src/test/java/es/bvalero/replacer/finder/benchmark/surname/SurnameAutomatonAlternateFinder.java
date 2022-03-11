package es.bvalero.replacer.finder.benchmark.surname;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

class SurnameAutomatonAlternateFinder implements BenchmarkFinder {

    private final RunAutomaton words;

    SurnameAutomatonAlternateFinder(Collection<String> words) {
        final String alternations = '(' + StringUtils.join(words, "|") + ')';
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public Set<BenchmarkResult> findMatches(WikipediaPage page) {
        String text = page.getContent();
        // Build an alternate automaton with all the words and match it against the text
        final Set<BenchmarkResult> matches = new HashSet<>();
        final AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            if (FinderUtils.isWordPrecededByUppercase(m.start(), m.group(), text)) {
                matches.add(BenchmarkResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }
}
