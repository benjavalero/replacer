package es.bvalero.replacer.finder.benchmark.person;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.*;

class PersonAutomatonCompleteFinder implements BenchmarkFinder {

    private final List<RunAutomaton> words;

    PersonAutomatonCompleteFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(
                    new RunAutomaton(new RegExp(word + "<Z><Lu>").toAutomaton(new DatatypesAutomatonProvider()))
                );
        }
    }

    @Override
    public Set<BenchmarkResult> findMatches(WikipediaPage page) {
        String text = page.getContent();
        // We loop over all the words and find them completely in the text with an automaton
        Set<BenchmarkResult> matches = new HashSet<>();
        for (RunAutomaton word : this.words) {
            AutomatonMatcher m = word.newMatcher(text);
            while (m.find()) {
                matches.add(BenchmarkResult.of(m.start(), m.group().substring(0, m.group().length() - 2)));
            }
        }
        return matches;
    }
}
