package es.bvalero.replacer.finder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CosmeticFindServiceTest {
    @Mock
    private List<CosmeticFinder> cosmeticFinders;

    @InjectMocks
    private CosmeticFindService cosmeticFindService;

    @Before
    public void setUp() {
        cosmeticFindService = new CosmeticFindService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testApplyCosmeticChanges() {
        Cosmetic cosmetic = Cosmetic.of(2, "[[Link|link]]", "[[link]]");
        CosmeticFinder finder = Mockito.mock(CosmeticFinder.class);
        Mockito.when(finder.findStream(Mockito.anyString())).thenReturn(Stream.of(cosmetic));
        Mockito.when(cosmeticFinders.iterator()).thenReturn(Collections.singletonList(finder).iterator());

        String text = "A [[Link|link]] to simplify.";
        String expected = "A [[link]] to simplify.";
        Assert.assertEquals(expected, cosmeticFindService.applyCosmeticChanges(text));
    }
}