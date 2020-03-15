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

public class DomainFinderTest {

    @Test
    public void testRegexFileName() {
        String domain1 = "IMDb.org";
        String domain2 = "es.wikipedia.org";
        String domain3 = "acb.es";
        String domain4 = "www.domain.com/index.php";
        String text = String.format("Entre %s, %s {{=%s}} [http://%s]", domain1, domain2, domain3, domain4);

        ImmutableFinder domainFinder = new DomainFinder();
        List<Immutable> matches = domainFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(domain1, domain2, domain3));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
