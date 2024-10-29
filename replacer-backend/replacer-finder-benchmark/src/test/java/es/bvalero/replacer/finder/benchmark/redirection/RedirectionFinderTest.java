package es.bvalero.replacer.finder.benchmark.redirection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = BenchmarkFinder.class)
class RedirectionFinderTest {

    @Autowired
    private FinderProperties finderProperties;

    private List<String> redirectionWords;

    @BeforeEach
    public void setUp() {
        this.redirectionWords = this.finderProperties.getRedirectionWords();
    }

    @Test
    void testRedirectionLowercaseContainsFinder() {
        RedirectionLowercaseContainsFinder finder = new RedirectionLowercaseContainsFinder(redirectionWords);
        doAssertions(finder);
    }

    @Test
    void testRedirectionRegexFinder() {
        RedirectionRegexFinder finder = new RedirectionRegexFinder(redirectionWords);
        doAssertions(finder);
    }

    @Test
    void testRedirectionRegexInsensitiveFinder() {
        RedirectionRegexInsensitiveFinder finder = new RedirectionRegexInsensitiveFinder(redirectionWords);
        doAssertions(finder);
    }

    @Test
    void testRedirectionAutomatonFinder() {
        RedirectionAutomatonFinder finder = new RedirectionAutomatonFinder(redirectionWords);
        doAssertions(finder);
    }

    @Test
    void testRedirectionAhoCorasickFinder() {
        RedirectionAhoCorasickFinder finder = new RedirectionAhoCorasickFinder(redirectionWords);
        doAssertions(finder);
    }

    @Test
    void testRedirectionAhoCorasickLongestFinder() {
        RedirectionAhoCorasickLongestFinder finder = new RedirectionAhoCorasickLongestFinder(redirectionWords);
        doAssertions(finder);
    }

    @Test
    void testRedirectionAhoCorasickWholeFinder() {
        RedirectionAhoCorasickWholeFinder finder = new RedirectionAhoCorasickWholeFinder(redirectionWords);
        doAssertions(finder);
    }

    @Test
    void testRedirectionAhoCorasickWholeLongestFinder() {
        RedirectionAhoCorasickWholeLongestFinder finder = new RedirectionAhoCorasickWholeLongestFinder(
            redirectionWords
        );
        doAssertions(finder);
    }

    private void doAssertions(BenchmarkFinder finder) {
        assertFalse(finder.findMatches("xxx #REDIRECCIÓN [[A]] yyy").isEmpty());
        assertFalse(finder.findMatches("xxx #redirección [[A]] yyy").isEmpty());
        assertFalse(finder.findMatches("xxx #REDIRECT [[A]] yyy").isEmpty());
        assertTrue(finder.findMatches("Otro contenido").isEmpty());
        assertTrue(finder.findMatches("xxx {{destruir|motivo}}").isEmpty());
    }
}
