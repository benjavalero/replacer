package es.bvalero.replacer.finder.benchmark.word;

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

class WordAutomatonAlternateFinder implements BenchmarkFinder {

    private final RunAutomaton automaton;

    WordAutomatonAlternateFinder(Collection<String> words) {
        String alternations = '(' + StringUtils.join(words, "|") + ')';
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public Set<BenchmarkResult> findMatches(WikipediaPage page) {
        String text = page.getContent();
        // Build an alternate automaton with all the words and match it against the text
        Set<BenchmarkResult> matches = new HashSet<>();
        AutomatonMatcher m = this.automaton.newMatcher(text);
        while (m.find()) {
            if (FinderUtils.isWordCompleteInText(m.start(), m.group(), text)) {
                matches.add(BenchmarkResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }
}
