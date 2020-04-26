package es.bvalero.replacer.finder.benchmark.filename;

import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.apache.commons.collections4.IterableUtils;
import org.intellij.lang.annotations.RegExp;

class FileRegexFinder implements BenchmarkFinder {
    @RegExp
    private static final String REGEX = "\\[\\[(?:Archivo|File|Imagen?):[^]|]+";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public Set<FinderResult> findMatches(String text) {
        return new HashSet<>(IterableUtils.toList(new RegexIterable<>(text, PATTERN, this::convert)));
    }

    @Override
    public FinderResult convert(MatchResult match) {
        String file = match.group();
        int colon = file.indexOf(':');
        return FinderResult.of(match.start() + colon + 1, file.substring(colon + 1));
    }
}
