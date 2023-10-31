package es.bvalero.replacer.page.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaService;
import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.CosmeticFindService;
import es.bvalero.replacer.finder.FinderPage;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplyCosmeticsServiceTest {

    // Dependency injection
    private CosmeticFindService cosmeticFindService;
    private CheckWikipediaService checkWikipediaService;

    private ApplyCosmeticsService applyCosmeticsService;

    @BeforeEach
    public void setUp() {
        cosmeticFindService = mock(CosmeticFindService.class);
        checkWikipediaService = mock(CheckWikipediaService.class);
        applyCosmeticsService = new ApplyCosmeticsService(cosmeticFindService, checkWikipediaService);
    }

    @Test
    void testApplyCosmeticChanges() {
        Cosmetic cosmetic = Cosmetic.builder().start(2).text("[[Link|link]]").fix("[[link]]").build();
        when(cosmeticFindService.findCosmetics(any(FinderPage.class))).thenReturn(Set.of(cosmetic));

        String text = "A [[Link|link]] to simplify.";
        String expected = "A [[link]] to simplify.";
        FinderPage page = buildFinderPage(text);
        assertEquals(expected, applyCosmeticsService.applyCosmeticChanges(page));

        verify(cosmeticFindService).findCosmetics(any(FinderPage.class));
        verify(checkWikipediaService)
            .reportFix(page.getPageKey().getLang(), page.getTitle(), cosmetic.getCheckWikipediaAction());
    }

    private FinderPage buildFinderPage(String content) {
        return FinderPage.of(content);
    }

    @Test
    void testApplySeveralCosmeticChanges() {
        Cosmetic cosmetic = Cosmetic.builder().start(2).text("[[Link|link]]").fix("[[link]]").build();
        Cosmetic cosmetic2 = Cosmetic.builder().start(29).text("archivo").fix("Archivo").build();
        Cosmetic cosmetic3 = Cosmetic.builder().start(19).text("</br>").fix("<br>").build();
        when(cosmeticFindService.findCosmetics(any(FinderPage.class)))
            .thenReturn(Set.of(cosmetic, cosmetic2, cosmetic3));

        String text = "A [[Link|link]] to </br> a [[archivo:x.jpeg]].";
        String expected = "A [[link]] to <br> a [[Archivo:x.jpeg]].";
        FinderPage page = buildFinderPage(text);
        assertEquals(expected, applyCosmeticsService.applyCosmeticChanges(page));

        verify(cosmeticFindService).findCosmetics(any(FinderPage.class));
        verify(checkWikipediaService, times(3))
            .reportFix(page.getPageKey().getLang(), page.getTitle(), cosmetic.getCheckWikipediaAction());
    }
}
