package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.common.domain.Immutable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
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
        String content = "En el hotel Hilton de Paris vivía París Hilton.";

        ImmutableFinder titleFinder = new TitleFinder();
        WikipediaPage page = WikipediaPage.of(WikipediaLanguage.getDefault(), content, title);
        List<Immutable> matches = IterableUtils.toList(titleFinder.find(page));

        // Use a list to find repeated results
        List<String> expected = List.of("Hilton", "Hilton", "Paris");
        List<String> actual = matches.stream().map(Immutable::getText).sorted().collect(Collectors.toList());
        assertEquals(expected, actual);
    }
}
