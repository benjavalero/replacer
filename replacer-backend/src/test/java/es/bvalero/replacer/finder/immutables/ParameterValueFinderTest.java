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

public class ParameterValueFinderTest {

    @Test
    public void testRegexParameterValue() {
        String value1 = "index=A\nvalue\n";
        String value2 = " índice = Another value ";
        String image = "imagen1=Archivo:xxx.jpg";
        String text = String.format("{{Template|%s|%s|%s}}", value1, value2, image);

        ImmutableFinder parameterValueFinder = new ParameterValueFinder();
        List<Immutable> matches = parameterValueFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(value1, value2, image));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
