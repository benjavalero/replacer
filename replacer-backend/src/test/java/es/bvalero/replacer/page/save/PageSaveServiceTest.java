package es.bvalero.replacer.page.save;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinderService;
import es.bvalero.replacer.page.review.PageReviewSearch;
import es.bvalero.replacer.page.review.ReviewPage;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaDateUtils;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageSaveServiceTest {

    private static final String EMPTY_CONTENT = " ";

    @Mock
    private ReplacementService replacementService;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private CosmeticFinderService cosmeticFinderService;

    @InjectMocks
    private PageSaveService pageSaveService;

    @BeforeEach
    public void setUp() {
        pageSaveService = new PageSaveService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testSaveWithChanges() throws ReplacerException {
        int pageId = 123;
        String title = "Q";
        String content = "X";
        LocalDateTime timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String token = "A";
        String tokenSecret = "B";
        PageSaveRequest request = new PageSaveRequest();
        ReviewPage reviewPage = new ReviewPage();
        reviewPage.setLang(WikipediaLanguage.SPANISH);
        reviewPage.setId(pageId);
        reviewPage.setTitle(title);
        reviewPage.setContent(content);
        reviewPage.setQueryTimestamp(WikipediaDateUtils.formatWikipediaTimestamp(timestamp));
        request.setPage(reviewPage);
        PageReviewSearch search = new PageReviewSearch();
        request.setSearch(search);
        request.setToken(token);
        request.setTokenSecret(tokenSecret);

        String contentAfterCosmetics = "C";
        when(cosmeticFinderService.applyCosmeticChanges(any(FinderPage.class))).thenReturn(contentAfterCosmetics);

        String user = "U";
        pageSaveService.savePageContent(user, request);

        verify(cosmeticFinderService).applyCosmeticChanges(any(FinderPage.class));
        verify(wikipediaService)
            .savePageContent(
                eq(WikipediaPageId.of(reviewPage.getLang(), reviewPage.getId())),
                isNull(),
                eq(contentAfterCosmetics),
                eq(timestamp),
                anyString(),
                eq(AccessToken.of(token, tokenSecret))
            );
        verify(replacementService).reviewByPageId(reviewPage.getLang(), reviewPage.getId(), null, null, user);
    }

    @Test
    void testSaveWithNoChanges() throws ReplacerException {
        int pageId = 123;
        String title = "Q";
        LocalDateTime timestamp = LocalDateTime.now();
        String token = "A";
        String tokenSecret = "B";
        PageSaveRequest request = new PageSaveRequest();
        ReviewPage reviewPage = new ReviewPage();
        reviewPage.setLang(WikipediaLanguage.SPANISH);
        reviewPage.setId(pageId);
        reviewPage.setTitle(title);
        reviewPage.setContent(EMPTY_CONTENT);
        reviewPage.setQueryTimestamp(WikipediaDateUtils.formatWikipediaTimestamp(timestamp));
        request.setPage(reviewPage);
        PageReviewSearch search = new PageReviewSearch();
        request.setSearch(search);
        request.setToken(token);
        request.setTokenSecret(tokenSecret);

        String user = "U";
        pageSaveService.savePageWithNoChanges(user, request);

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
        verify(replacementService).reviewByPageId(reviewPage.getLang(), reviewPage.getId(), null, null, user);
    }
}
