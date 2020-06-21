package es.bvalero.replacer.finder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
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
        Mockito.when(finder.findStream(Mockito.anyString())).thenReturn(Stream.of(cosmetic));
        Mockito.when(cosmeticFinders.iterator()).thenReturn(Collections.singletonList(finder).iterator());

        String text = "A [[Link|link]] to simplify.";
        String expected = "A [[link]] to simplify.";
        Assertions.assertEquals(expected, cosmeticFindService.applyCosmeticChanges(text));
    }
}
