package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;

class IgnorableTemplateLowercaseContainsFinder implements BenchmarkFinder {

    private final List<String> ignorableTemplates = new ArrayList<>();

    IgnorableTemplateLowercaseContainsFinder(Set<String> ignorableTemplates) {
        this.ignorableTemplates.addAll(ignorableTemplates);
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        String lowerCaseText = FinderUtils.toLowerCase(text);
        for (String ignorableTemplate : ignorableTemplates) {
            int start = lowerCaseText.indexOf(ignorableTemplate);
            if (start >= 0 && FinderUtils.isWordCompleteInText(start, ignorableTemplate, lowerCaseText)) {
                return Set.of(BenchmarkResult.of(0, text));
            }
        }
        return Collections.emptySet();
    }
}
