package es.bvalero.replacer.page.save;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.User;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageSaveCommand;
import es.bvalero.replacer.wikipedia.WikipediaPageSaveRepository;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageSaveServiceTest {

    // Dependency injection
    private CosmeticApi cosmeticApi;
    private EditionsPerMinuteValidator editionsPerMinuteValidator;
    private WikipediaPageSaveRepository wikipediaPageSaveRepository;
    private PageSaveRepository pageSaveRepository;

    private PageSaveService pageSaveService;

    @BeforeEach
    public void setUp() {
        cosmeticApi = mock(CosmeticApi.class);
        editionsPerMinuteValidator = mock(EditionsPerMinuteValidator.class);
        wikipediaPageSaveRepository = mock(WikipediaPageSaveRepository.class);
        pageSaveRepository = mock(PageSaveRepository.class);
        pageSaveService = new PageSaveService(
            cosmeticApi,
            editionsPerMinuteValidator,
            wikipediaPageSaveRepository,
            pageSaveRepository
        );
    }

    @Test
    void testSaveWithChanges() throws WikipediaException {
        int pageId = 123;
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), pageId);
        String reviewer = "X";

        ReviewedReplacement reviewedReplacement = ReviewedReplacement.builder()
            .pageKey(pageKey)
            .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .reviewer(reviewer)
            .fixed(true)
            .build();
        ReviewedPage reviewedPage = ReviewedPage.builder()
            .pageKey(pageKey)
            .title("T")
            .content("X")
            .queryTimestamp(WikipediaTimestamp.now())
            .reviewedReplacements(List.of(reviewedReplacement))
            .build();

        when(cosmeticApi.applyCosmeticChanges(any(FinderPage.class))).thenReturn(reviewedPage.toFinderPage());
        User user = User.buildTestUser();
        pageSaveService.save(reviewedPage, user);

        verify(cosmeticApi).applyCosmeticChanges(any(FinderPage.class));

        WikipediaPageSaveCommand pageSave = WikipediaPageSaveCommand.builder()
            .pageKey(reviewedPage.getPageKey())
            .content(reviewedPage.getContent())
            .editSummary(EditSummaryBuilder.build(List.of(reviewedReplacement.getType()), false))
            .queryTimestamp(reviewedPage.getQueryTimestamp())
            .build();

        verify(wikipediaPageSaveRepository).save(pageSave, user.getAccessToken());
        verify(pageSaveRepository).save(anyCollection());
    }

    @Test
    void testSaveWithoutChanges() throws WikipediaException {
        int pageId = 123;
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), pageId);
        String reviewer = "X";

        ReviewedReplacement r1 = ReviewedReplacement.builder()
            .pageKey(pageKey)
            .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .reviewer(reviewer)
            .build();
        ReviewedReplacement r2 = ReviewedReplacement.builder()
            .pageKey(pageKey)
            .type(StandardType.of(ReplacementKind.COMPOSED, "2"))
            .start(2)
            .reviewer(reviewer)
            .build();
        ReviewedReplacement r3 = ReviewedReplacement.builder()
            .pageKey(pageKey)
            .type(CustomType.of("3", false))
            .start(3)
            .reviewer(reviewer)
            .build();
        Collection<ReviewedReplacement> reviewedReplacements = List.of(r1, r2, r3);

        ReviewedPage reviewedPage = ReviewedPage.builder()
            .pageKey(pageKey)
            .reviewedReplacements(reviewedReplacements)
            .build();

        User user = User.buildTestUser();
        pageSaveService.save(reviewedPage, user);

        verify(wikipediaPageSaveRepository, never()).save(any(WikipediaPageSaveCommand.class), any(AccessToken.class));
        verify(pageSaveRepository).save(Collections.singletonList(reviewedPage.toIndexedPage(null)));
    }
}
