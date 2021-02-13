package es.bvalero.replacer.finder.benchmark.template;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.common.FinderPage;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class TemplateRegexAlternateFinder implements BenchmarkFinder {

    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+?}}";
    private static final String REGEX_NESTED = "\\{\\{\\s*(%s)\\s*[|:](%s|[^}])+?}}";
    private final Pattern pattern;

    TemplateRegexAlternateFinder(List<String> words) {
        this.pattern =
            Pattern.compile(String.format(REGEX_NESTED, StringUtils.join(toUpperCase(words), '|'), REGEX_TEMPLATE));
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), pattern);
    }
}
