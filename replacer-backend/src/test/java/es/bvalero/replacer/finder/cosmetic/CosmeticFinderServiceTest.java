package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.finder.common.FinderPage;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class CosmeticFinderServiceTest {

    @Mock
    private List<CosmeticFinder> cosmeticFinders;

    @Mock
    private CheckWikipediaService checkWikipediaService;

    @InjectMocks
    private CosmeticFinderService cosmeticFinderService;

    @BeforeEach
    public void setUp() {
        cosmeticFinderService = new CosmeticFinderService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testApplyCosmeticChanges() {
        CosmeticFinder finder = Mockito.mock(CosmeticFinder.class);
        Cosmetic cosmetic = Cosmetic.builder().start(2).text("[[Link|link]]").finder(finder).fix("[[link]]").build();
        List<Cosmetic> cosmetics = new LinkedList<>(); // To be able to sort it
        cosmetics.add(cosmetic);
        Mockito.when(finder.find(Mockito.any(FinderPage.class))).thenReturn(cosmetics);
        Mockito.when(cosmeticFinders.toArray()).thenReturn(new CosmeticFinder[] { finder });

        String text = "A [[Link|link]] to simplify.";
        String expected = "A [[link]] to simplify.";
        FinderPage page = FinderPage.of(text);
        Assertions.assertEquals(expected, cosmeticFinderService.applyCosmeticChanges(page));
    }
}
