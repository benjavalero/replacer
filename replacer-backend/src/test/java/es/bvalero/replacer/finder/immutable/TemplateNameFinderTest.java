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

public class TemplateNameFinderTest {

    @Test
    public void testRegexTemplateName() {
        String template1 = " Plantilla 1\n";
        String template2 = "Plantilla\n 2";
        String template3 = "Plantilla-3";
        String text = String.format("{{%s| 1 }} {{%s}} {{%s:3}}", template1, template2, template3);

        ImmutableFinder templateNameFinder = new TemplateNameFinder();
        List<Immutable> matches = templateNameFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(template1, template2, template3));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assert.assertEquals(expected, actual);
    }
}
