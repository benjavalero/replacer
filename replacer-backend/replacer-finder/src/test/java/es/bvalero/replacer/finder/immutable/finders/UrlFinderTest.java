package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UrlFinderTest {

    private UrlFinder urlFinder;

    @BeforeEach
    public void setUp() {
        urlFinder = new UrlFinder();
    }

    @Test
    void testUrl() {
        String url1 = "https://google.es?u=aj&t2+rl=http://www.marca.com#!page~2,3";
        String url2 = "http://www.marca.com";
        String text = String.format("[%s Google] [%s Marca]", url1, url2);

        List<Immutable> matches = urlFinder.findList(text);

        Set<String> expected = Set.of(url1, url2);
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "https://www.google.es",
            "http://www.google.es",
            "https://es.wikipedia.org/wiki/Art%C3%ADculo",
            "https://www.example.com/path/to/page",
            "https://example.com/search?q=hello+world&lang=es",
            "https://example.com/page#section",
            "https://user:pass@example.com",
            "https://example.com:8080/path",
        }
    )
    void testValidUrls(String url) {
        List<Immutable> matches = urlFinder.findList(url);

        assertEquals(1, matches.size());
        assertEquals(url, matches.get(0).text());
    }

    @ParameterizedTest
    @ValueSource(strings = { "not a url", "ftp://example.com", "example.com" })
    void testNonUrls(String text) {
        List<Immutable> matches = urlFinder.findList(text);

        assertTrue(matches.isEmpty());
    }

    @Test
    void testUrlNotExtendedBeyondDelimiters() {
        // URLs inside wiki markup should not include the surrounding characters
        assertEquals("https://example.com", urlFinder.findList("[https://example.com texto]").get(0).text());
        assertEquals("https://example.com", urlFinder.findList("{{param|https://example.com}}").get(0).text());
        assertEquals("https://example.com", urlFinder.findList("<ref>https://example.com</ref>").get(0).text());
    }
}
