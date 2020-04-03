package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TemplateParamFinderTest {

    @Test
    public void testRegexTemplateParam() {
        String param1 = " param 1 ";
        String param2 = "\tparám_2\t";
        String param3 = "param-3";
        String param4 = "param4";
        String text = String.format("{{Template|%s= value1 |%s= value2 |%s=|%s}}", param1, param2, param3, param4);

        ImmutableFinder templateParamFinder = new TemplateParamFinder();
        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(param1, param2, param3));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testLinkFollowedByHeader() {
        String text = "[[A|B]]\n==Section==";

        ImmutableFinder templateParamFinder = new TemplateParamFinder();
        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    public void testTableRowWithReference() {
        String text = "{|\n" + "|-\n" + "| Text\n" + "| More text.<ref name=\"FDA1\">Reference</ref>\n" + "|}";

        ImmutableFinder templateParamFinder = new TemplateParamFinder();
        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    public void testCiteTemplate() {
        String text = "{{cita|Text.<ref>[http://www.britannica.com/Elegy#ref=ref945156].</ref>}}";

        ImmutableFinder templateParamFinder = new TemplateParamFinder();
        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertTrue(actual.isEmpty());
    }
}
