package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

class IgnorableTemplateRegexFinder implements BenchmarkFinder {

    private final Pattern pattern;

    IgnorableTemplateRegexFinder(Set<String> ignorableTemplates) {
        Set<String> fixedTemplates = ignorableTemplates
            .stream()
            .map(s -> FinderUtils.toLowerCase(s.replace("{", "\\{")))
            .collect(Collectors.toSet());
        String alternations = '(' + StringUtils.join(fixedTemplates, "|") + ')';
        this.pattern = Pattern.compile(alternations);
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        Set<BenchmarkResult> matches = new HashSet<>();
        String lowerCaseText = FinderUtils.toLowerCase(text);
        Matcher m = this.pattern.matcher(FinderUtils.toLowerCase(lowerCaseText));
        while (m.find()) {
            if (FinderUtils.isWordCompleteInText(m.start(), m.group(), lowerCaseText)) {
                matches.add(BenchmarkResult.of(0, text));
            }
        }
        return matches;
    }
}
