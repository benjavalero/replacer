package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class TemplateParamFinderTest {

    @Test
    public void testRegexTemplateParam() {
        String param1 = "param 1";
        String param2 = "parám_2";
        String param3 = "param-3";
        String param4 = "param4";
        String link = "[[A|B]]\n==Section==";
        String text = String.format(
            "{{Template| %s = value1 |\t%s\t= value2 |%s=|%s}} %s",
            param1,
            param2,
            param3,
            param4,
            link
        );

        ImmutableFinder templateParamFinder = new TemplateParamFinder();
        List<Immutable> matches = templateParamFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(param1, param2, param3));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assert.assertEquals(expected, actual);
    }
}
