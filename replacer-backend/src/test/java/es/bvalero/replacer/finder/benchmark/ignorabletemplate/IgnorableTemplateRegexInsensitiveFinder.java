package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

class IgnorableTemplateRegexInsensitiveFinder implements BenchmarkFinder {

    private final Pattern pattern;

    IgnorableTemplateRegexInsensitiveFinder(Set<String> ignorableTemplates) {
        Set<String> fixedTemplates = ignorableTemplates
            .stream()
            .map(s -> s.replace("{", "\\{"))
            .collect(Collectors.toSet());
        String alternations = '(' + StringUtils.join(fixedTemplates, "|") + ')';
        this.pattern = Pattern.compile(alternations, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    @Override
    public Set<BenchmarkResult> findMatches(FinderPage page) {
        String text = page.getContent();
        Set<BenchmarkResult> matches = new HashSet<>();
        Matcher m = this.pattern.matcher(text);
        while (m.find()) {
            if (FinderUtils.isWordCompleteInText(m.start(), m.group(), text)) {
                matches.add(BenchmarkResult.of(0, text));
            }
        }
        return matches;
    }
}
