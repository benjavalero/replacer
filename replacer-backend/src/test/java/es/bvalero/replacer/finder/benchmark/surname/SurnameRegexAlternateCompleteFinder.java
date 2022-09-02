package es.bvalero.replacer.finder.benchmark.surname;

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

class SurnameRegexAlternateCompleteFinder implements BenchmarkFinder {

    private final Pattern words;

    SurnameRegexAlternateCompleteFinder(Collection<String> words) {
        String alternations = "\\p{Lu}\\p{L}+ (" + FinderUtils.joinAlternate(words) + ")";
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Set<BenchmarkResult> find(WikipediaPage page) {
        String text = page.getContent();
        // Build an alternate regex with all the complete words and match it against the text
        final Set<BenchmarkResult> matches = new HashSet<>();
        final Matcher m = this.words.matcher(text);
        while (m.find()) {
            final int pos = m.group().indexOf(' ') + 1;
            final int start = m.start() + pos;
            final String matchText = m.group().substring(pos);
            if (FinderUtils.isWordCompleteInText(start, matchText, text)) {
                matches.add(BenchmarkResult.of(start, matchText));
            }
        }
        return matches;
    }
}
