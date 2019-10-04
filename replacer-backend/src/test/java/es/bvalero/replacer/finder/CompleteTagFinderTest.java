package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CompleteTagFinderTest {

    @Test
    public void testRegexCompleteTag() {
        String tag1 = "<math class=\"latex\">Un <i>ejemplo</i>\n en LaTeX</math>";
        String tag2 = "<math>Otro ejemplo</math>";
        String tag3 = "<source>Otro ejemplo</source>";
        String tag4 = "<ref name=NH05/>";
        String text = String.format("En %s %s %s %s.", tag1, tag2, tag3, tag4);

        IgnoredReplacementFinder completeTagFinder = new CompleteTagFinder();

        List<IgnoredReplacement> matches = completeTagFinder.findIgnoredReplacements(text);
        Assert.assertEquals(3, matches.size());

        Set<String> expected = new HashSet<>(Arrays.asList(tag1, tag2, tag3));
        Set<String> actual = matches.stream().map(IgnoredReplacement::getText).collect(Collectors.toSet());
        Assert.assertEquals(expected, actual);
    }

}
