package es.bvalero.replacer.finder.benchmark.completetag;

import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;

class CompleteTagRegexIteratedFinder implements BenchmarkFinder {

    private List<Pattern> patterns = new ArrayList<>();

    CompleteTagRegexIteratedFinder(Set<String> tags) {
        patterns.addAll(
            tags
                .stream()
                .map(tag -> Pattern.compile(String.format("<%s[^>/]*?>.+?</%s>", tag, tag), Pattern.DOTALL))
                .collect(Collectors.toList())
        );
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        WikipediaPage page = WikipediaPage.builder().content(text).lang(WikipediaLanguage.getDefault()).build();
        return patterns
            .stream()
            .flatMap(pattern -> IterableUtils.toList(new RegexIterable<>(page, pattern, this::convert)).stream())
            .collect(Collectors.toSet());
    }
}
