package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.Immutable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { CompleteTagFinder.class, XmlConfiguration.class })
public class CompleteTagFinderTest {
    @Autowired
    private CompleteTagFinder completeTagFinder;

    @Test
    public void testRegexCompleteTag() {
        String tag1 = "<math class=\"latex\">An <i>example</i>\n in LaTeX</math>";
        String tag2 = "<math>To test repeated tags</math>";
        String tag3 = "<source>Another example</source>";
        String tag4 = "<ref name=NH05/>";
        String tag5 = "<ref>Text</ref>";
        String tag6 = "<unknown>Unknown</unknown>";
        String tag7 = "<ref>Unclosed tag";
        String text = String.format("En %s %s %s %s %s %s %s", tag1, tag2, tag3, tag4, tag5, tag6, tag7);

        List<Immutable> matches = completeTagFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(tag1, tag2, tag3, tag5));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
