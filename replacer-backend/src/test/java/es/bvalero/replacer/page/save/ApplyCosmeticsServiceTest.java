package es.bvalero.replacer.page.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.Cosmetic;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinderService;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ApplyCosmeticsServiceTest {

    @Mock
    private CosmeticFinderService cosmeticFinderService;

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
        when(cosmeticFinderService.find(any(FinderPage.class))).thenReturn(Collections.singletonList(cosmetic));

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

        verify(cosmeticFinderService).find(any(FinderPage.class));
    }
}
