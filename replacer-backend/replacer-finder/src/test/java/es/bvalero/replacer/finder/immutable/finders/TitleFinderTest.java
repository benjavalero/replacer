package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import java.util.stream.Collectors;
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

        FinderPage page = FinderPage.of(title, content);
        List<Immutable> matches = titleFinder.find(page).toList();

        // Use a list to find repeated results
        List<String> expected = List.of("Hilton", "Hilton", "Paris");
        List<String> actual = matches.stream().map(Immutable::getText).sorted().collect(Collectors.toList());
        assertEquals(expected, actual);
    }
}
