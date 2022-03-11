package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.HashSet;
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
        String alternations = '(' + StringUtils.join(fixedTemplates, "|") + ')';
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public Set<BenchmarkResult> findMatches(WikipediaPage page) {
        String text = page.getContent();
        Set<BenchmarkResult> matches = new HashSet<>();
        String lowerCaseText = FinderUtils.toLowerCase(text);
        AutomatonMatcher m = this.automaton.newMatcher(lowerCaseText);
        while (m.find()) {
            if (FinderUtils.isWordCompleteInText(m.start(), m.group(), lowerCaseText)) {
                matches.add(BenchmarkResult.of(0, text));
            }
        }
        return matches;
    }
}
