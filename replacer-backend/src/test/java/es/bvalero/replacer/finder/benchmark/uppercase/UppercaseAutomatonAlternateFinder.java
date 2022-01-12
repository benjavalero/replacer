package es.bvalero.replacer.finder.benchmark.uppercase;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

class UppercaseAutomatonAlternateFinder extends UppercaseBenchmarkFinder {

    private final RunAutomaton words;

    UppercaseAutomatonAlternateFinder(Collection<String> words) {
        String alternations = String.format(
            "(%s)<Zs>*(%s)",
            StringUtils.join(PUNCTUATIONS, "|"),
            StringUtils.join(words, "|")
        );
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    @Override
    public Set<BenchmarkResult> findMatches(FinderPage page) {
        String text = page.getContent();
        // Build an alternate automaton with all the words and match it against the text
        Set<BenchmarkResult> matches = new HashSet<>();
        AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            String word = m.group().substring(1).trim();
            int pos = m.group().indexOf(word);
            matches.add(BenchmarkResult.of(m.start() + pos, word));
        }
        return matches;
    }
}
