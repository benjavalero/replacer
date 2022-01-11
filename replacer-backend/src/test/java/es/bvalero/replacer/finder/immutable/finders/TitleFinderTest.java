package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.Test;

class TitleFinderTest {

    @Test
    void testTitleFinder() {
        String title = "11 Paris, Hilton";
        String content = "En el hotel hilton de Paris.";

        ImmutableFinder titleFinder = new TitleFinder();
        FinderPage page = FinderPage.of(WikipediaLanguage.getDefault(), content, title);
        List<Immutable> matches = IterableUtils.toList(titleFinder.find(page));

        Set<String> expected = Set.of("Hilton", "Paris");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }
}
