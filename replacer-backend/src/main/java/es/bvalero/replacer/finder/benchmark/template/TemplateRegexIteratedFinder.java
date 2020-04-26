package es.bvalero.replacer.finder.benchmark.template;

import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;

class TemplateRegexIteratedFinder implements BenchmarkFinder {
    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+?}}";
    private static final String REGEX_NESTED = "\\{\\{\\s*%s\\s*[|:](%s|[^}])+?}}";
    private static final List<Pattern> PATTERNS = new ArrayList<>();

    TemplateRegexIteratedFinder(List<String> templateNames) {
        PATTERNS.addAll(
            toUpperCase(templateNames)
                .stream()
                .map(name -> Pattern.compile(String.format(REGEX_NESTED, name, REGEX_TEMPLATE)))
                .collect(Collectors.toList())
        );
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        for (Pattern pattern : PATTERNS) {
            matches.addAll(IterableUtils.toList(new RegexIterable<>(text, pattern, this::convert)));
        }
        return matches;
    }
}
