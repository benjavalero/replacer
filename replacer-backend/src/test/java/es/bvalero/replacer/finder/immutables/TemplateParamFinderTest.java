package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.Immutable;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { TemplateParamFinder.class, XmlConfiguration.class })
class TemplateParamFinderTest {
    @Autowired
    private TemplateParamFinder templateParamFinder;

    @Test
    void testRegexTemplateParam() {
        String param1 = " param 1 ";
        String param2 = "\tparám_2\t";
        String param3 = "param-3";
        String param4 = "param4";
        String text = String.format("{{Template|%s= value1 |%s= value2 |%s=|%s}}", param1, param2, param3, param4);

        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(param1, param2, param3));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testLinkFollowedByHeader() {
        String text = "[[A|B]]\n==Section==";

        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testTableRowWithReference() {
        String text = "{|\n" + "|-\n" + "| Text\n" + "| More text.<ref name=\"FDA1\">Reference</ref>\n" + "|}";

        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testTableRowWithStylesInQuotes() {
        String text = "{| color=\"salmon\"\n" + "| Text |}";

        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testParamValueWithQuotes() {
        String text = "{{Template|param=Division \"A\"}}";

        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertFalse(actual.isEmpty());
    }

    @Test
    void testTableRowWithDash() {
        String text = "{|- align=center\n" + "| Text |}";

        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testTableRowWithKnownAttribute() {
        String param = "bgcolor=salmon\n";
        String text = String.format("{|\n" + "|%s" + "| Text |}", param);

        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> expected = Collections.singleton(param);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testCiteTemplate() {
        String text = "{{cita|Text.<ref>[http://www.britannica.com/Elegy#ref=ref945156].</ref>}}";

        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testFileNameValue() {
        String fileValueParam = "mapa = Parroquia san agustin. - libertador.svg\n";
        String text = String.format("{{Ficha de entidad subnacional\n" + "|%s" + "}}", fileValueParam);

        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> expected = Collections.singleton(fileValueParam);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testFileParameter() {
        String fileParam = "link";
        String text = String.format("[[File:x.jpg|%s=x]]", fileParam);

        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> expected = Collections.singleton(fileParam);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testValueWithComment() {
        String completeParam = "image = x.jpg ";
        String text = String.format("{{Template|%s<!-- A comment -->}}", completeParam);

        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> expected = Collections.singleton(completeParam);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testCiteValue() {
        String param = "ps";
        String text = "{{P|ps= «Libro Nº 34, año 1825, f. 145).»x}}";

        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> expected = Collections.singleton(param);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
