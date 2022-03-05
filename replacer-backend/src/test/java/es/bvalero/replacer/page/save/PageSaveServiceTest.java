package es.bvalero.replacer.page.save;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.page.review.PageReviewOptions;
import es.bvalero.replacer.repository.CustomRepository;
import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
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
    private PageRepository pageRepository;

    @Mock
    private CustomRepository customRepository;

    @Mock
    private WikipediaPageRepository wikipediaPageRepository;

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
        verify(wikipediaPageRepository)
            .save(
                eq(page.getId()),
                isNull(),
                eq(contentAfterCosmetics),
                eq(timestamp),
                anyString(),
                eq(accessToken)
            );
        verify(replacementTypeRepository).updateReviewerByPageAndType(page.getId(), null, "A");
    }

    @Test
    void testSaveWithNoChangesNoType() throws WikipediaException {
        int pageId = 123;
        PageReviewOptions options = PageReviewOptions.ofNoType();
        WikipediaPageId wikipediaPageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId);
        pageSaveService.savePageWithNoChanges(wikipediaPageId, options);

        verify(applyCosmeticsService, never()).applyCosmeticChanges(any(WikipediaPage.class));
        verify(wikipediaPageRepository, never())
            .save(
                any(WikipediaPageId.class),
                anyInt(),
                anyString(),
                any(LocalDateTime.class),
                anyString(),
                any(AccessToken.class)
            );
        verify(replacementTypeRepository).updateReviewerByPageAndType(wikipediaPageId, null, options.getUser());
    }

    @Test
    void testSaveWithNoChangesByType() throws WikipediaException {
        int pageId = 123;
        ReplacementType type = ReplacementType.of(ReplacementKind.DATE, "S");
        PageReviewOptions options = PageReviewOptions.ofType(type);
        WikipediaPageId wikipediaPageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId);
        pageSaveService.savePageWithNoChanges(wikipediaPageId, options);

        verify(applyCosmeticsService, never()).applyCosmeticChanges(any(WikipediaPage.class));
        verify(wikipediaPageRepository, never())
            .save(
                any(WikipediaPageId.class),
                anyInt(),
                anyString(),
                any(LocalDateTime.class),
                anyString(),
                any(AccessToken.class)
            );
        verify(replacementTypeRepository, never())
            .updateReviewerByPageAndType(wikipediaPageId, null, options.getUser());
        verify(replacementTypeRepository)
            .updateReviewerByPageAndType(wikipediaPageId, options.getType(), options.getUser());
    }

    @Test
    void testSaveWithNoChangesByTypeReviewAll() throws WikipediaException {
        int pageId = 123;
        ReplacementType type = ReplacementType.of(ReplacementKind.DATE, "S");
        PageReviewOptions options = PageReviewOptions.ofType(type).withReviewAllTypes(true);
        WikipediaPageId wikipediaPageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId);
        pageSaveService.savePageWithNoChanges(wikipediaPageId, options);

        verify(applyCosmeticsService, never()).applyCosmeticChanges(any(WikipediaPage.class));
        verify(wikipediaPageRepository, never())
            .save(
                any(WikipediaPageId.class),
                anyInt(),
                anyString(),
                any(LocalDateTime.class),
                anyString(),
                any(AccessToken.class)
            );
        verify(replacementTypeRepository, never())
            .updateReviewerByPageAndType(wikipediaPageId, options.getType(), options.getUser());
        verify(replacementTypeRepository).updateReviewerByPageAndType(wikipediaPageId, null, options.getUser());
    }

    @Test
    void testSaveWithNoChangesByCustom() throws WikipediaException {
        int pageId = 123;
        PageReviewOptions options = PageReviewOptions.ofCustom(WikipediaLanguage.getDefault(), "S", "S2", true);
        WikipediaPageId wikipediaPageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId);
        pageSaveService.savePageWithNoChanges(wikipediaPageId, options);

        verify(applyCosmeticsService, never()).applyCosmeticChanges(any(WikipediaPage.class));
        verify(wikipediaPageRepository, never())
            .save(
                any(WikipediaPageId.class),
                anyInt(),
                anyString(),
                any(LocalDateTime.class),
                anyString(),
                any(AccessToken.class)
            );
        verify(replacementTypeRepository, never())
            .updateReviewerByPageAndType(wikipediaPageId, null, options.getUser());
        verify(customRepository).updateReviewerByPageAndType(wikipediaPageId, "S", true, options.getUser());
    }

    @Test
    void testSaveWithNoChangesByCustomReviewAll() throws WikipediaException {
        int pageId = 123;
        PageReviewOptions options = PageReviewOptions
            .ofCustom(WikipediaLanguage.getDefault(), "S", "S2", true)
            .withReviewAllTypes(true);
        WikipediaPageId wikipediaPageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId);
        pageSaveService.savePageWithNoChanges(wikipediaPageId, options);

        verify(applyCosmeticsService, never()).applyCosmeticChanges(any(WikipediaPage.class));
        verify(wikipediaPageRepository, never())
            .save(
                any(WikipediaPageId.class),
                anyInt(),
                anyString(),
                any(LocalDateTime.class),
                anyString(),
                any(AccessToken.class)
            );
        verify(replacementTypeRepository).updateReviewerByPageAndType(wikipediaPageId, null, options.getUser());
        verify(customRepository).updateReviewerByPageAndType(wikipediaPageId, "S", true, options.getUser());
    }
}
