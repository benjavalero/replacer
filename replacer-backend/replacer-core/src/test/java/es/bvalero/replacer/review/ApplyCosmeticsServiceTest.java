package es.bvalero.replacer.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaService;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.CosmeticFindService;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ApplyCosmeticsServiceTest {

    @Mock
    private CosmeticFindService cosmeticFindService;

    @Mock
    private CheckWikipediaService checkWikipediaService;

    @InjectMocks
    private ApplyCosmeticsService applyCosmeticsService;

    @BeforeEach
    public void setUp() {
        applyCosmeticsService = new ApplyCosmeticsService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testApplyCosmeticChanges() {
        Cosmetic cosmetic = Cosmetic.builder().start(2).text("[[Link|link]]").fix("[[link]]").build();
        when(cosmeticFindService.findCosmetics(any(WikipediaPage.class))).thenReturn(Collections.singleton(cosmetic));

        String text = "A [[Link|link]] to simplify.";
        String expected = "A [[link]] to simplify.";
        WikipediaPage page = buildWikipediaPage(text);
        assertEquals(expected, applyCosmeticsService.applyCosmeticChanges(page));

        verify(cosmeticFindService).findCosmetics(any(WikipediaPage.class));
        verify(checkWikipediaService)
            .reportFix(page.getPageKey().getLang(), page.getTitle(), cosmetic.getCheckWikipediaAction());
    }

    private WikipediaPage buildWikipediaPage(String content) {
        return WikipediaPage
            .builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
            .namespace(WikipediaNamespace.getDefault())
            .title("T")
            .content(content)
            .lastUpdate(LocalDateTime.now())
            .queryTimestamp(LocalDateTime.now())
            .build();
    }

    @Test
    void testApplySeveralCosmeticChanges() {
        Cosmetic cosmetic = Cosmetic.builder().start(2).text("[[Link|link]]").fix("[[link]]").build();
        Cosmetic cosmetic2 = Cosmetic.builder().start(29).text("archivo").fix("Archivo").build();
        Cosmetic cosmetic3 = Cosmetic.builder().start(19).text("</br>").fix("<br>").build();
        when(cosmeticFindService.findCosmetics(any(WikipediaPage.class)))
            .thenReturn(Set.of(cosmetic, cosmetic2, cosmetic3));

        String text = "A [[Link|link]] to </br> a [[archivo:x.jpeg]].";
        String expected = "A [[link]] to <br> a [[Archivo:x.jpeg]].";
        WikipediaPage page = buildWikipediaPage(text);
        assertEquals(expected, applyCosmeticsService.applyCosmeticChanges(page));

        verify(cosmeticFindService).findCosmetics(any(WikipediaPage.class));
        verify(checkWikipediaService, times(3))
            .reportFix(page.getPageKey().getLang(), page.getTitle(), cosmetic.getCheckWikipediaAction());
    }
}
