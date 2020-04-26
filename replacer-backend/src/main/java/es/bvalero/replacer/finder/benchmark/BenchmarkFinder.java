package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.FinderUtils;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;

public interface BenchmarkFinder {
    Set<FinderResult> findMatches(String text);

    default FinderResult convert(MatchResult match) {
        return FinderResult.of(match.start(), match.group());
    }

    default List<String> toUpperCase(List<String> names) {
        return names.stream().map(this::toUpperCase).collect(Collectors.toList());
    }

    private String toUpperCase(String word) {
        return FinderUtils.startsWithLowerCase(word) ? FinderUtils.setFirstUpperCaseClass(word) : word;
    }
}
