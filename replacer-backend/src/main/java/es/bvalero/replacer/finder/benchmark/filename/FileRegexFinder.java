package es.bvalero.replacer.finder.benchmark.filename;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;

class FileRegexFinder implements BenchmarkFinder {
    @RegExp
    private static final String REGEX = "\\[\\[(?:Archivo|File|Imagen?):[^]|]+";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        Matcher m = PATTERN.matcher(text);
        while (m.find()) {
            String file = m.group();
            int colon = file.indexOf(':');
            matches.add(FinderResult.of(m.start() + colon + 1, file.substring(colon + 1)));
        }
        return matches;
    }
}
