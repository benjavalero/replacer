package es.bvalero.replacer.finder.benchmark.template;

import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;

class TemplateRegexAllFinder implements BenchmarkFinder {
    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+?}}";
    private static final String REGEX_NESTED = "\\{\\{\\s*(%s)\\s*[|:](%s|[^}])+?}}";
    private Pattern pattern;

    TemplateRegexAllFinder(List<String> words) {
        this.pattern =
            Pattern.compile(String.format(REGEX_NESTED, StringUtils.join(toUpperCase(words), '|'), REGEX_TEMPLATE));
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        return new HashSet<>(IterableUtils.toList(new RegexIterable<>(text, pattern, this::convert)));
    }
}
