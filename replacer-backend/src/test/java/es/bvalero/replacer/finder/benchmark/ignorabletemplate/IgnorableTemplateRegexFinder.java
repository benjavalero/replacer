package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class IgnorableTemplateRegexFinder implements BenchmarkFinder {

    private final Pattern pattern;

    IgnorableTemplateRegexFinder(Set<String> ignorableTemplates) {
        Set<String> fixedTemplates = ignorableTemplates
            .stream()
            .map(s -> FinderUtils.toLowerCase(s.replace("{", "\\{")))
            .collect(Collectors.toSet());
        String alternations = '(' + FinderUtils.joinAlternate(fixedTemplates) + ')';
        this.pattern = Pattern.compile(alternations);
    }

    @Override
    public Iterable<BenchmarkResult> find(WikipediaPage page) {
        final String text = page.getContent();
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final String lowerCaseText = FinderUtils.toLowerCase(text);
        final Matcher m = this.pattern.matcher(lowerCaseText);
        while (m.find()) {
            if (FinderUtils.isWordCompleteInText(m.start(), m.group(), lowerCaseText)) {
                matches.add(BenchmarkResult.of(0, text));
            }
        }
        return matches;
    }
}
