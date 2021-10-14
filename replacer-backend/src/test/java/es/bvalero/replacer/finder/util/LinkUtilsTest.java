package es.bvalero.replacer.finder.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.FinderPage;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class LinkUtilsTest {

    @Test
    void testFindLink() {
        String link = "[[Link|Text]]";

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = LinkUtils.findAllLinks(page);

        assertEquals(1, matches.size());
        assertEquals(link, matches.get(0).group());
    }

    @Test
    void testFindLinkTruncated() {
        String link = "[[Link|Text";

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = LinkUtils.findAllLinks(page);

        assertTrue(matches.isEmpty());
    }

    @Test
    void testNestedLink() {
        String link2 = "[[Link2|Text2]]";
        String link = String.format("[[Link|Text %s Text]]", link2);

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = LinkUtils.findAllLinks(page);

        assertEquals(2, matches.size());
        assertEquals(link, matches.get(0).group());
        assertEquals(link2, matches.get(1).group());
    }

    @Test
    void testNestedLinks() {
        String link3 = "[[Link3|Text3]]";
        String link2 = "[[Link2|Text2]]";
        String link = String.format("[[Link|Text %s Text %s Text]]", link2, link3);

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = LinkUtils.findAllLinks(page);

        Set<String> links = Set.of(link, link2, link3);
        assertEquals(links, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedLink2() {
        String link3 = "[[Link3|Text3]]";
        String link2 = String.format("[[Link2|Text2 %s Text2]]", link3);
        String link = String.format("[[Link|Text %s Text]]", link2);

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = LinkUtils.findAllLinks(page);

        assertEquals(3, matches.size());
        assertEquals(link, matches.get(0).group());
        assertEquals(link2, matches.get(1).group());
        assertEquals(link3, matches.get(2).group());
    }

    @Test
    void testNestedLink2Truncated() {
        String link3 = "[[Link3|Text3]]";
        String link2 = String.format("[[Link2|Text2 %s Text2]]", link3);
        String link = String.format("[[Link|Text %s Text", link2);

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = LinkUtils.findAllLinks(page);

        assertEquals(2, matches.size());
        assertEquals(link2, matches.get(0).group());
        assertEquals(link3, matches.get(1).group());
    }

    @Test
    void testFindLinks() {
        String link1 = "[[Link1|Text1]]";
        String link2 = "[[Link2|Text2]]";
        String text = String.format("%s %s", link1, link2);

        FinderPage page = FinderPage.of(text);
        List<LinearMatchResult> matches = LinkUtils.findAllLinks(page);

        List<String> expected = List.of(link1, link2);
        assertEquals(expected, matches.stream().map(MatchResult::group).collect(Collectors.toList()));
    }
}
