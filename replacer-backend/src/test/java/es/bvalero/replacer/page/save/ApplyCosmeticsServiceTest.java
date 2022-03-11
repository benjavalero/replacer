package es.bvalero.replacer.page.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.Cosmetic;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinderService;
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
    private CosmeticFinderService cosmeticFinderService;

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
        when(cosmeticFinderService.find(any(WikipediaPage.class))).thenReturn(Collections.singleton(cosmetic));

        String text = "A [[Link|link]] to simplify.";
        String expected = "A [[link]] to simplify.";
        WikipediaPage page = WikipediaPage
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), 1))
            .namespace(WikipediaNamespace.getDefault())
            .title("T")
            .content(text)
            .lastUpdate(LocalDateTime.now())
            .build();
        assertEquals(expected, applyCosmeticsService.applyCosmeticChanges(page));

        verify(cosmeticFinderService).find(any(WikipediaPage.class));
        verify(checkWikipediaService)
            .reportFix(page.getId().getLang(), page.getTitle(), cosmetic.getCheckWikipediaAction());
    }

    @Test
    void testApplySeveralCosmeticChanges() {
        Cosmetic cosmetic = Cosmetic.builder().start(2).text("[[Link|link]]").fix("[[link]]").build();
        Cosmetic cosmetic2 = Cosmetic.builder().start(29).text("archivo").fix("Archivo").build();
        Cosmetic cosmetic3 = Cosmetic.builder().start(19).text("</br>").fix("<br>").build();
        when(cosmeticFinderService.find(any(WikipediaPage.class))).thenReturn(Set.of(cosmetic, cosmetic2, cosmetic3));

        String text = "A [[Link|link]] to </br> a [[archivo:x.jpeg]].";
        String expected = "A [[link]] to <br> a [[Archivo:x.jpeg]].";
        WikipediaPage page = WikipediaPage
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), 1))
            .namespace(WikipediaNamespace.getDefault())
            .title("T")
            .content(text)
            .lastUpdate(LocalDateTime.now())
            .build();
        assertEquals(expected, applyCosmeticsService.applyCosmeticChanges(page));

        verify(cosmeticFinderService).find(any(WikipediaPage.class));
        verify(checkWikipediaService, times(3))
            .reportFix(page.getId().getLang(), page.getTitle(), cosmetic.getCheckWikipediaAction());
    }
}
