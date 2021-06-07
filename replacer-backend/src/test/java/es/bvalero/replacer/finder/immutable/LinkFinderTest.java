package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { LinkFinder.class, XmlConfiguration.class })
class LinkFinderTest {

    @Autowired
    private LinkFinder linkFinder;

    @Test
    void testFindLink() {
        String link = "[[Link|Text]]";

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = linkFinder.findAllLinks(page);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(link, matches.get(0).group());
    }

    @Test
    void testFindLinkTruncated() {
        String link = "[[Link|Text";

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = linkFinder.findAllLinks(page);

        Assertions.assertTrue(matches.isEmpty());
    }

    @Test
    void testNestedLink() {
        String link2 = "[[Link2|Text2]]";
        String link = String.format("[[Link|Text %s Text]]", link2);

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = linkFinder.findAllLinks(page);

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
        List<LinearMatchResult> matches = linkFinder.findAllLinks(page);

        Set<String> links = Set.of(link, link2, link3);
        Assertions.assertEquals(links, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedLink2() {
        String link3 = "[[Link3|Text3]]";
        String link2 = String.format("[[Link2|Text2 %s Text2]]", link3);
        String link = String.format("[[Link|Text %s Text]]", link2);

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = linkFinder.findAllLinks(page);

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
        List<LinearMatchResult> matches = linkFinder.findAllLinks(page);

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
        List<LinearMatchResult> matches = linkFinder.findAllLinks(page);

        List<String> expected = List.of(link1, link2);
        Assertions.assertEquals(expected, matches.stream().map(MatchResult::group).collect(Collectors.toList()));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "[[Categoría:Países]]", "[[Categoría:Obras españolas|Españolas, Obras]]", "[[Categoría:Informática| ]]",
        }
    )
    void testFindCategories(String text) {
        List<Immutable> matches = linkFinder.findList(text);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(text, matches.get(0).getText());
    }

    @Test
    void testLinkSuffixed() {
        String suffixed1 = "[[brasil]]eño";
        String suffixed2 = "[[reacción química|reaccion]]es";
        String noSuffixed = "[[Text]]";
        String inLink = "[[totem]]s";
        String nested = String.format("[[Los %s]]", inLink);
        String text = String.format("%s, %s, %s y %s", suffixed1, nested, suffixed2, noSuffixed);

        List<Immutable> matches = linkFinder.findList(text);

        Set<String> expected = Set.of(suffixed1, suffixed2, inLink);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testLinkAliased() {
        String aliased1 = "brasil";
        String noAliased = "Text";
        String aliased2 = " reacción química ";
        String withNewLine = "one\nexample";
        String file = "File:file.jpg";
        String aliasedAnnex = "Anexo:Países";
        String category = "[[Categoría:Países| ]]"; // This is captured entirely
        String interWiki = "s:es:Corán";
        String text = String.format(
            "[[%s|Brasil]] [[%s]] [[%s|reacción]] [[%s|example]] [[%s|thumb]] [[%s|Países]] %s [[%s|Corán]].",
            aliased1,
            noAliased,
            aliased2,
            withNewLine,
            file,
            aliasedAnnex,
            category,
            interWiki
        );

        List<Immutable> matches = linkFinder.findList(text);

        Set<String> expected = Set.of(aliased1, aliased2, withNewLine, file, aliasedAnnex, category, interWiki);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testFindFileName() {
        String filename2 = "a b.png";
        String image = String.format("[[Imagen:%s]]", filename2);
        String filename3 = "Z.JPEG";
        String fileLowercase = String.format("[[archivo:%s]]", filename3);
        String text = String.format("%s %s", image, fileLowercase);

        List<Immutable> matches = linkFinder.findList(text);

        Set<String> expected = Set.of(image, fileLowercase);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testFileAliasedWithParameters() {
        String fileWithParameters = "Archivo:xxx.jpg|link=yyy";
        String text = String.format("[[%s|alias]]", fileWithParameters);

        List<Immutable> matches = linkFinder.findList(text);

        Set<String> expected = Set.of(fileWithParameters);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testParameterAlias() {
        String text = "[[Archivo:xxx.jpg|link=yyy]]";

        List<Immutable> matches = linkFinder.findList(text);

        Set<String> expected = Set.of(text);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "[[sv:Renkavle]]",
            "[[fr:Compression de données#Compression avec pertes]]",
            "[[zh:浮游生物]]",
            "[[:en:Constitution of Virginia]]",
        }
    )
    void testFindInterLanguageLink(String text) {
        List<Immutable> matches = linkFinder.findList(text);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(text, matches.get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "[[s:es:Corán|Corán]]", "[[dc:Tierra Uno|Tierra- 1]]" })
    void testFindInterWikiAliased(String text) {
        List<Immutable> matches = linkFinder.findList(text);

        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertFalse(actual.contains(text));
    }
}
