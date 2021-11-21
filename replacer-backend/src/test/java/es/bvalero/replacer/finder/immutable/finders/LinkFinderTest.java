package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { LinkFinder.class, XmlConfiguration.class })
class LinkFinderTest {

    @Autowired
    private LinkFinder linkFinder;

    @ParameterizedTest
    @ValueSource(
        strings = {
            "[[Categoría:Países]]",
            "[[Categoría:Obras españolas|Españolas, Obras]]",
            "[[Categoría:Informática| ]]",
            "[[Category:Asia]]",
            "[[categoría:Asia]]",
        }
    )
    void testFindCategories(String text) {
        List<Immutable> matches = linkFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(text, matches.get(0).getText());
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
        assertEquals(expected, actual);
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
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(strings = { "[[Imagen:a b.png]]", "[[archivo:Z.JPEG]]", "[[File:x.jpg]]", "[[Image:z.pdf]]" })
    void testFindFileNames(String text) {
        List<Immutable> matches = linkFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(text, matches.get(0).getText());
    }

    @Test
    void testFileAliasedWithParameters() {
        String fileWithParameters = "Archivo:xxx.jpg|link=yyy";
        String text = String.format("[[%s|alias]]", fileWithParameters);

        List<Immutable> matches = linkFinder.findList(text);

        Set<String> expected = Set.of(fileWithParameters);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testParameterAlias() {
        String text = "[[Archivo:xxx.jpg|link=yyy]]";

        List<Immutable> matches = linkFinder.findList(text);

        Set<String> expected = Set.of(text);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
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

        assertEquals(1, matches.size());
        assertEquals(text, matches.get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "[[s:es:Corán|Corán]]", "[[dc:Tierra Uno|Tierra- 1]]" })
    void testFindInterWikiAliased(String text) {
        List<Immutable> matches = linkFinder.findList(text);

        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertFalse(actual.contains(text));
    }

    @ParameterizedTest
    @ValueSource(strings = { "[[Arquivo:xxx.jpg]]", "[[Ficheiro:y.pdf]]", "[[File:z.png]]", "[[imaxe:a.jpeg]]" })
    void testGalicianFile(String text) {
        List<Immutable> matches = IterableUtils.toList(
            linkFinder.find(FinderPage.of(WikipediaLanguage.GALICIAN, text, "X"))
        );

        assertEquals(1, matches.size());
        assertEquals(text, matches.get(0).getText());
    }
}
