package es.bvalero.replacer.finder.benchmark.redirection;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.List;
import java.util.stream.Stream;

/**
 * Finds the redirection words in the lowercase text using an automaton alternation.
 * Returns the whole page on first match.
 * Case-insensitive: normalizes keywords to lowercase in the constructor and converts the text to lowercase at search time.
 * Note: {@code #} must be escaped as {@code \#} in dk.brics.automaton regex syntax.
 */
class RedirectionAutomatonFinder implements BenchmarkFinder {

    private final RunAutomaton automaton;

    RedirectionAutomatonFinder(List<String> redirectionWords) {
        List<String> fixedRedirectionWords = redirectionWords
            .stream()
            .map(ReplacerUtils::toLowerCase)
            .map(s -> s.replace("#", "\\#"))
            .toList();
        String alternations = '(' + FinderUtils.joinAlternate(fixedRedirectionWords) + ')';
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final String lowerCaseText = ReplacerUtils.toLowerCase(text);
        final AutomatonMatcher m = this.automaton.newMatcher(lowerCaseText);
        while (m.find()) {
            if (FinderUtils.isWordCompleteInText(m, lowerCaseText)) {
                return Stream.of(BenchmarkResult.of(0, text));
            }
        }
        return Stream.of();
    }
}
