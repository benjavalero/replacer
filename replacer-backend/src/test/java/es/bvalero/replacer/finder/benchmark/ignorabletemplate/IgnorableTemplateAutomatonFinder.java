package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

class IgnorableTemplateAutomatonFinder implements BenchmarkFinder {

    private final RunAutomaton automaton;

    IgnorableTemplateAutomatonFinder(Set<String> ignorableTemplates) {
        Set<String> fixedTemplates = ignorableTemplates
            .stream()
            .map(s -> s.replace("{", "\\{"))
            .map(s -> s.replace("#", "\\#"))
            .map(FinderUtils::toLowerCase)
            .collect(Collectors.toSet());
        String alternations = '(' + FinderUtils.joinAlternate(fixedTemplates) + ')';
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public Iterable<BenchmarkResult> find(WikipediaPage page) {
        final String text = page.getContent();
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final String lowerCaseText = FinderUtils.toLowerCase(text);
        final AutomatonMatcher m = this.automaton.newMatcher(lowerCaseText);
        while (m.find()) {
            if (FinderUtils.isWordCompleteInText(m.start(), m.group(), lowerCaseText)) {
                matches.add(BenchmarkResult.of(0, text));
            }
        }
        return matches;
    }
}
