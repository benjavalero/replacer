package es.bvalero.replacer.finder;

import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class CosmeticFindServiceTest {

    @Mock
    private List<CosmeticFinder> cosmeticFinders;

    @InjectMocks
    private CosmeticFindService cosmeticFindService;

    @BeforeEach
    public void setUp() {
        cosmeticFindService = new CosmeticFindService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testApplyCosmeticChanges() {
        Cosmetic cosmetic = Cosmetic.of(2, "[[Link|link]]", "[[link]]");
        CosmeticFinder finder = Mockito.mock(CosmeticFinder.class);
        List<Cosmetic> cosmetics = new LinkedList<>(); // To be able to sort it
        cosmetics.add(cosmetic);
        Mockito.when(finder.findList(Mockito.anyString())).thenReturn(cosmetics);
        Mockito.when(cosmeticFinders.iterator()).thenReturn(Collections.singletonList(finder).iterator());

        String text = "A [[Link|link]] to simplify.";
        String expected = "A [[link]] to simplify.";
        WikipediaPage page = WikipediaPage.builder().content(text).build();
        Assertions.assertEquals(expected, cosmeticFindService.applyCosmeticChanges(page));
    }
}
