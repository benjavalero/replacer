package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TitleFinderTest {

    private TitleFinder titleFinder;

    @BeforeEach
    public void setUp() {
        titleFinder = new TitleFinder();
    }

    @Test
    void testTitle() {
        String title = "11 Paris, Hilton";
        String content = "En el hotel Hilton de Paris vivía París Hilton.";

        FinderPage page = FinderPage.of(WikipediaLanguage.getDefault(), title, content);
        List<Immutable> matches = IterableUtils.toList(titleFinder.find(page));

        // Use a list to find repeated results
        List<String> expected = List.of("Hilton", "Hilton", "Paris");
        List<String> actual = matches.stream().map(Immutable::getText).sorted().collect(Collectors.toList());
        assertEquals(expected, actual);
    }
}
