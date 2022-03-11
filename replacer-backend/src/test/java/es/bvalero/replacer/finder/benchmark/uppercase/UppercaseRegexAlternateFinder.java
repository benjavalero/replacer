package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class UppercaseRegexAlternateFinder extends UppercaseBenchmarkFinder {

    private final Pattern words;

    UppercaseRegexAlternateFinder(Collection<String> words) {
        String alternations = String.format(
            "(?:%s)\\s*(?:%s)",
            StringUtils.join(PUNCTUATIONS, "|"),
            StringUtils.join(words, "|")
        );
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Set<BenchmarkResult> findMatches(WikipediaPage page) {
        String text = page.getContent();
        // Build an alternate regex with all the words and match it against the text
        Set<BenchmarkResult> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            String w = m.group().substring(1).trim();
            int pos = m.group().indexOf(w);
            matches.add(BenchmarkResult.of(m.start() + pos, w));
        }
        return matches;
    }
}
