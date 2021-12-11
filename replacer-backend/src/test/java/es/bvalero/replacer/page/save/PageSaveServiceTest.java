package es.bvalero.replacer.page.save;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinderService;
import es.bvalero.replacer.page.review.PageReviewOptions;
import es.bvalero.replacer.repository.CustomRepository;
import es.bvalero.replacer.repository.ReplacementRepository;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageSaveServiceTest {

    @Mock
    private ReplacementRepository replacementRepository;

    @Mock
    private CustomRepository customRepository;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private CosmeticFinderService cosmeticFinderService;

    @InjectMocks
    private PageSaveService pageSaveService;

    @BeforeEach
    public void setUp() {
        pageSaveService = new PageSaveService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveWithChanges() throws ReplacerException {
        int pageId = 123;
        LocalDateTime timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        WikipediaPage page = WikipediaPage
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId))
            .namespace(WikipediaNamespace.getDefault()) // Not relevant for saving
            .title("T")
            .content("X")
            .lastUpdate(timestamp)
            .queryTimestamp(timestamp)
            .build();

        String contentAfterCosmetics = "C";
        when(cosmeticFinderService.applyCosmeticChanges(any(FinderPage.class))).thenReturn(contentAfterCosmetics);

        AccessToken accessToken = AccessToken.of("A", "B");
        pageSaveService.savePageContent(page, null, PageReviewOptions.ofNoType(), accessToken);

        verify(cosmeticFinderService).applyCosmeticChanges(any(FinderPage.class));
        verify(wikipediaService)
            .savePageContent(
                eq(page.getId()),
                isNull(),
                eq(contentAfterCosmetics),
                eq(timestamp),
                anyString(),
                eq(accessToken)
            );
        verify(replacementRepository)
            .updateReviewerByPageAndType(WikipediaLanguage.getDefault(), pageId, null, null, "A");
    }

    @Test
    void testSaveWithNoChanges() throws ReplacerException {
        int pageId = 123;
        String type = "T";
        String subtype = "S";
        pageSaveService.savePageWithNoChanges(pageId, PageReviewOptions.ofTypeSubtype(type, subtype));

        verify(cosmeticFinderService, never()).applyCosmeticChanges(any(FinderPage.class));
        verify(wikipediaService, never())
            .savePageContent(
                any(WikipediaPageId.class),
                anyInt(),
                anyString(),
                any(LocalDateTime.class),
                anyString(),
                any(AccessToken.class)
            );
        verify(replacementRepository)
            .updateReviewerByPageAndType(WikipediaLanguage.getDefault(), pageId, type, subtype, "A");
    }
}
