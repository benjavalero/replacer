package es.bvalero.replacer.page.save;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.page.review.PageReviewOptions;
import es.bvalero.replacer.repository.CustomRepository;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import es.bvalero.replacer.wikipedia.WikipediaException;
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
    private ReplacementTypeRepository replacementTypeRepository;

    @Mock
    private CustomRepository customRepository;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private ApplyCosmeticsService applyCosmeticsService;

    @InjectMocks
    private PageSaveService pageSaveService;

    @BeforeEach
    public void setUp() {
        pageSaveService = new PageSaveService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveWithChanges() throws WikipediaException {
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
        when(applyCosmeticsService.applyCosmeticChanges(page)).thenReturn(contentAfterCosmetics);

        AccessToken accessToken = AccessToken.of("A", "B");
        pageSaveService.savePageContent(page, null, PageReviewOptions.ofNoType(), accessToken);

        verify(applyCosmeticsService).applyCosmeticChanges(page);
        verify(wikipediaService)
            .savePageContent(
                eq(page.getId()),
                isNull(),
                eq(contentAfterCosmetics),
                eq(timestamp),
                anyString(),
                eq(accessToken)
            );
        verify(replacementTypeRepository)
            .updateReviewerByPageAndType(WikipediaLanguage.getDefault(), pageId, null, "A");
    }

    @Test
    void testSaveWithNoChanges() throws WikipediaException {
        int pageId = 123;
        ReplacementType type = ReplacementType.of(ReplacementKind.DATE, "S");
        PageReviewOptions options = PageReviewOptions.ofType(type);
        pageSaveService.savePageWithNoChanges(pageId, options);

        verify(applyCosmeticsService, never()).applyCosmeticChanges(any(WikipediaPage.class));
        verify(wikipediaService, never())
            .savePageContent(
                any(WikipediaPageId.class),
                anyInt(),
                anyString(),
                any(LocalDateTime.class),
                anyString(),
                any(AccessToken.class)
            );
        verify(replacementTypeRepository)
            .updateReviewerByPageAndType(
                WikipediaLanguage.getDefault(),
                pageId,
                options.getReplacementType(),
                options.getUser()
            );
    }
}
