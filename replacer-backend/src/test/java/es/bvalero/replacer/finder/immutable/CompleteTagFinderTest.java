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

public class CompleteTagFinderTest {

    @Test
    public void testRegexCompleteTag() {
        String tag1 = "<math class=\"latex\">Un <i>ejemplo</i>\n en LaTeX</math>";
        String tag2 = "<math>Otro ejemplo</math>";
        String tag3 = "<source>Otro ejemplo</source>";
        String tag4 = "<ref name=NH05/>";
        String tag5 = "<ref>Text</ref>";
        String text = String.format("En %s %s %s %s %s.", tag1, tag2, tag3, tag4, tag5);

        ImmutableFinder completeTagFinder = new CompleteTagFinder();
        List<Immutable> matches = completeTagFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(tag1, tag2, tag3, tag5));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assert.assertEquals(expected, actual);
    }
}
