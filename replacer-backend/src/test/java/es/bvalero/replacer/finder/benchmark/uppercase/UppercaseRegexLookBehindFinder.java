package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class UppercaseRegexLookBehindFinder extends UppercaseBenchmarkFinder {

    private final List<Pattern> words;

    UppercaseRegexLookBehindFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            String regex = String.format("(?<=%s)\\s*%s", StringUtils.join(PUNCTUATIONS, "|"), word);
            this.words.add(Pattern.compile(regex));
        }
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        // We loop over all the words and find them in the text with a regex
        Set<BenchmarkResult> matches = new HashSet<>();
        for (Pattern word : this.words) {
            Matcher m = word.matcher(text);
            while (m.find()) {
                String w = m.group().trim();
                int pos = m.group().indexOf(w);
                matches.add(BenchmarkResult.of(m.start() + pos, w));
            }
        }
        return matches;
    }
}
