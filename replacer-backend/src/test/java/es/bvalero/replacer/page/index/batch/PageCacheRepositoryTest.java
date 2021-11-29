package es.bvalero.replacer.page.index.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.page.repository.PageModel;
import es.bvalero.replacer.page.repository.PageRepository;
import es.bvalero.replacer.page.repository.ReplacementModel;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageCacheRepositoryTest {

    @Mock
    private PageRepository pageRepository;

    @InjectMocks
    private PageCacheRepository PageModelCacheRepository;

    @BeforeEach
    public void setUp() {
        PageModelCacheRepository = new PageCacheRepository();
        PageModelCacheRepository.setChunkSize(1000);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindDatabaseReplacements() {
        // In DB: replacements for page 2 (first load) and 1001 (second load)
        // We ask for the page 1 and 1001, so the page 2 will be cleaned.
        Integer pageId1 = 2;
        ReplacementModel replacement1 = ReplacementModel
            .builder()
            .lang(WikipediaLanguage.getDefault())
            .pageId(pageId1)
            .type("")
            .subtype("")
            .position(0)
            .context("")
            .lastUpdate(LocalDate.now())
            .build();
        PageModel page1 = PageModel
            .builder()
            .lang(WikipediaLanguage.getDefault())
            .pageId(pageId1)
            .replacements(List.of(replacement1))
            .build();
        Integer pageId2 = 1001;
        ReplacementModel replacement2 = ReplacementModel
            .builder()
            .lang(WikipediaLanguage.getDefault())
            .pageId(pageId2)
            .type("")
            .subtype("")
            .position(0)
            .context("")
            .lastUpdate(LocalDate.now())
            .build();
        PageModel page2 = PageModel
            .builder()
            .lang(WikipediaLanguage.getDefault())
            .pageId(pageId2)
            .replacements(List.of(replacement2))
            .build();
        when(pageRepository.findByPageIdInterval(WikipediaLanguage.getDefault(), 1, 1000)).thenReturn(List.of(page1));
        when(pageRepository.findByPageIdInterval(WikipediaLanguage.getDefault(), 1001, 2000))
            .thenReturn(List.of(page2));

        Optional<PageModel> PageModelDB = PageModelCacheRepository.findByPageId(
            WikipediaPageId.of(WikipediaLanguage.getDefault(), 1)
        );
        assertTrue(PageModelDB.isEmpty());

        PageModelDB = PageModelCacheRepository.findByPageId(WikipediaPageId.of(WikipediaLanguage.getDefault(), 1001));
        assertEquals(page2, PageModelDB.orElse(null));

        // Check that the page 2 has been cleaned
        verify(pageRepository).deletePages(anyCollection());
    }

    @Test
    void testFindDatabaseReplacementsWithEmptyLoad() {
        // In DB: replacement for page 1001
        // So the first load is enlarged
        Integer pageId = 1001;
        ReplacementModel replacement = ReplacementModel
            .builder()
            .lang(WikipediaLanguage.getDefault())
            .pageId(pageId)
            .type("")
            .subtype("")
            .position(0)
            .context("")
            .lastUpdate(LocalDate.now())
            .build();
        PageModel page = PageModel
            .builder()
            .lang(WikipediaLanguage.getDefault())
            .pageId(pageId)
            .replacements(List.of(replacement))
            .build();
        when(pageRepository.findByPageIdInterval(WikipediaLanguage.getDefault(), 1, 2000)).thenReturn(List.of(page));

        Optional<PageModel> PageModelDB = PageModelCacheRepository.findByPageId(
            WikipediaPageId.of(WikipediaLanguage.getDefault(), 1001)
        );
        assertEquals(page, PageModelDB.orElse(null));
    }
}
