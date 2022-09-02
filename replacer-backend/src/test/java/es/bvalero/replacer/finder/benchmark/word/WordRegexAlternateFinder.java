package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class WordRegexAlternateFinder implements BenchmarkFinder {

    private final Pattern pattern;

    WordRegexAlternateFinder(Collection<String> words) {
        String alternations = '(' + FinderUtils.joinAlternate(words) + ')';
        this.pattern = Pattern.compile(alternations);
    }

    @Override
    public Set<BenchmarkResult> find(WikipediaPage page) {
        String text = page.getContent();
        // Build an alternate regex with all the words and match it against the text
        Set<BenchmarkResult> matches = new HashSet<>();
        Matcher m = this.pattern.matcher(text);
        while (m.find()) {
            if (FinderUtils.isWordCompleteInText(m.start(), m.group(), text)) {
                matches.add(BenchmarkResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }
}
