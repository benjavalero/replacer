package es.bvalero.replacer.cosmetic;

import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CosmeticChangesServiceTest {
    @Mock
    private List<CosmeticFinder> cosmeticFinders;

    @InjectMocks
    private CosmeticChangesService cosmeticChangesService;

    @Before
    public void setUp() {
        cosmeticChangesService = new CosmeticChangesService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testApplyCosmeticChanges() {
        CosmeticFinder finder = new SameLinkFinder();
        Mockito.when(cosmeticFinders.iterator()).thenReturn(Collections.singletonList(finder).iterator());

        String text = "A [[Link|link]] to simplify.";
        String expected = "A [[link]] to simplify.";
        Assert.assertEquals(expected, cosmeticChangesService.applyCosmeticChanges(text));
    }
}
