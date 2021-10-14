package es.bvalero.replacer.finder.cosmetic;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.finder.FinderPage;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CosmeticFinderServiceTest {

    @Mock
    private List<CosmeticFinder> cosmeticFinders;

    @InjectMocks
    private CosmeticFinderService cosmeticFinderService;

    @BeforeEach
    public void setUp() {
        cosmeticFinderService = new CosmeticFinderService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testApplyCosmeticChanges() {
        CosmeticFinder finder = mock(CosmeticFinder.class);
        Cosmetic cosmetic = Cosmetic.builder().start(2).text("[[Link|link]]").fix("[[link]]").build();
        List<Cosmetic> cosmetics = new LinkedList<>(); // To be able to sort it
        cosmetics.add(cosmetic);
        when(finder.find(any(FinderPage.class))).thenReturn(cosmetics);
        when(cosmeticFinders.toArray()).thenReturn(new CosmeticFinder[] { finder });

        String text = "A [[Link|link]] to simplify.";
        String expected = "A [[link]] to simplify.";
        FinderPage page = FinderPage.of(text);
        Assertions.assertEquals(expected, cosmeticFinderService.applyCosmeticChanges(page));
    }
}
