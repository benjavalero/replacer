package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.finder.FinderPage;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LinkUtilsTest {

    @Test
    void testFindLink() {
        String link = "[[Link|Text]]";

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = LinkUtils.findAllLinks(page);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(link, matches.get(0).group());
    }

    @Test
    void testFindLinkTruncated() {
        String link = "[[Link|Text";

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = LinkUtils.findAllLinks(page);

        Assertions.assertTrue(matches.isEmpty());
    }

    @Test
    void testNestedLink() {
        String link2 = "[[Link2|Text2]]";
        String link = String.format("[[Link|Text %s Text]]", link2);

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = LinkUtils.findAllLinks(page);

        Assertions.assertEquals(2, matches.size());
        Assertions.assertEquals(link, matches.get(0).group());
        Assertions.assertEquals(link2, matches.get(1).group());
    }

    @Test
    void testNestedLinks() {
        String link3 = "[[Link3|Text3]]";
        String link2 = "[[Link2|Text2]]";
        String link = String.format("[[Link|Text %s Text %s Text]]", link2, link3);

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = LinkUtils.findAllLinks(page);

        Set<String> links = Set.of(link, link2, link3);
        Assertions.assertEquals(links, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedLink2() {
        String link3 = "[[Link3|Text3]]";
        String link2 = String.format("[[Link2|Text2 %s Text2]]", link3);
        String link = String.format("[[Link|Text %s Text]]", link2);

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = LinkUtils.findAllLinks(page);

        Assertions.assertEquals(3, matches.size());
        Assertions.assertEquals(link, matches.get(0).group());
        Assertions.assertEquals(link2, matches.get(1).group());
        Assertions.assertEquals(link3, matches.get(2).group());
    }

    @Test
    void testNestedLink2Truncated() {
        String link3 = "[[Link3|Text3]]";
        String link2 = String.format("[[Link2|Text2 %s Text2]]", link3);
        String link = String.format("[[Link|Text %s Text", link2);

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = LinkUtils.findAllLinks(page);

        Assertions.assertEquals(2, matches.size());
        Assertions.assertEquals(link2, matches.get(0).group());
        Assertions.assertEquals(link3, matches.get(1).group());
    }

    @Test
    void testFindLinks() {
        String link1 = "[[Link1|Text1]]";
        String link2 = "[[Link2|Text2]]";
        String text = String.format("%s %s", link1, link2);

        FinderPage page = FinderPage.of(text);
        List<LinearMatchResult> matches = LinkUtils.findAllLinks(page);

        List<String> expected = List.of(link1, link2);
        Assertions.assertEquals(expected, matches.stream().map(MatchResult::group).collect(Collectors.toList()));
    }
}
