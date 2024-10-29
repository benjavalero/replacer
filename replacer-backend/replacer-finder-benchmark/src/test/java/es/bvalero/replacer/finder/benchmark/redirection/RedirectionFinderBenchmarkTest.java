package es.bvalero.replacer.finder.benchmark.redirection;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = BenchmarkFinder.class)
class RedirectionFinderBenchmarkTest extends BaseFinderBenchmark {

    private static final String fileName = "redirection/redirection-benchmark.csv";

    @Autowired
    private FinderProperties finderProperties;

    @Test
    void testBenchmark() throws ReplacerException {
        List<String> redirectionWords = this.finderProperties.getRedirectionWords();

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new RedirectionLowercaseContainsFinder(redirectionWords));
        finders.add(new RedirectionRegexFinder(redirectionWords));
        finders.add(new RedirectionRegexInsensitiveFinder(redirectionWords));
        finders.add(new RedirectionAutomatonFinder(redirectionWords));

        // Use the Aho-Corasick algorithm which eventually creates an automaton
        // In this case, the case-insensitive option is quite better than case-sensitive,
        // but not better than the simple index-of approach.
        // It seems the Aho-Corasick algorithm is only worth when searching lots of words.
        finders.add(new RedirectionAhoCorasickFinder(redirectionWords));
        finders.add(new RedirectionAhoCorasickLongestFinder(redirectionWords));
        finders.add(new RedirectionAhoCorasickWholeFinder(redirectionWords));
        finders.add(new RedirectionAhoCorasickWholeLongestFinder(redirectionWords));

        List<Finder<?>> benchmarkFinders = new ArrayList<>(finders);
        runBenchmark(benchmarkFinders, fileName);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "Redirection");
    }
}
