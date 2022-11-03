package es.bvalero.replacer.finder.benchmark.redirection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { XmlConfiguration.class })
class RedirectionFinderTest {

    @Resource
    private Set<String> ignorableTemplates;

    @Test
    void testRedirectionLowercaseContainsFinder() {
        RedirectionLowercaseContainsFinder finder = new RedirectionLowercaseContainsFinder(ignorableTemplates);
        doAssertions(finder);
    }

    @Test
    void testRedirectionRegexFinder() {
        RedirectionRegexFinder finder = new RedirectionRegexFinder(ignorableTemplates);
        doAssertions(finder);
    }

    @Test
    void testRedirectionRegexInsensitiveFinder() {
        RedirectionRegexInsensitiveFinder finder = new RedirectionRegexInsensitiveFinder(ignorableTemplates);
        doAssertions(finder);
    }

    @Test
    void testRedirectionAutomatonFinder() {
        RedirectionAutomatonFinder finder = new RedirectionAutomatonFinder(ignorableTemplates);
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
