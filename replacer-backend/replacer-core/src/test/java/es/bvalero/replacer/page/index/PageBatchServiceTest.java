package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.page.PageSaveRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageBatchServiceTest {

    // Dependency injection
    private PageRepository pageRepository;
    private PageSaveRepository pageSaveRepository;

    private PageBatchService pageBatchService;

    @BeforeEach
    public void setUp() {
        pageRepository = mock(PageRepository.class);
        pageSaveRepository = mock(PageSaveRepository.class);
        pageBatchService = new PageBatchService(pageRepository, pageSaveRepository);
        pageBatchService.setChunkSize(1000);
    }

    private IndexedPage buildIndexedPage(int pageId) {
        return IndexedPage.builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), pageId))
            .title("T" + pageId)
            .lastUpdate(LocalDate.now())
            .build();
    }

    @Test
    void testFindDatabaseReplacements() {
        // In DB: replacements for page 2 (first load) and 1001 (second load)
        // We ask for the page 1 and 1001, so the page 2 will be cleaned.
        int pageId1 = 2;
        IndexedPage page1 = buildIndexedPage(pageId1);

        int pageId2 = 1001;
        IndexedPage page2 = buildIndexedPage(pageId2);

        when(pageRepository.findByIdRange(WikipediaLanguage.getDefault(), 1, 1000)).thenReturn(List.of(page1));
        when(pageRepository.findByIdRange(WikipediaLanguage.getDefault(), 1001, 2000)).thenReturn(List.of(page2));

        Optional<IndexedPage> pageDb = pageBatchService.findByKey(PageKey.of(WikipediaLanguage.getDefault(), 1));
        assertTrue(pageDb.isEmpty());

        pageDb = pageBatchService.findByKey(PageKey.of(WikipediaLanguage.getDefault(), 1001));
        assertEquals(page2, pageDb.orElse(null));

        // Check that the page 2 has been cleaned
        verify(pageSaveRepository).removeByKey(anyCollection());
    }

    @Test
    void testFindDatabaseReplacementsWithEmptyLoad() {
        // In DB: replacement for page 1001
        // So the first load is enlarged
        int pageId = 1001;
        IndexedPage page = buildIndexedPage(pageId);
        when(pageRepository.findByIdRange(WikipediaLanguage.getDefault(), 1, 2000)).thenReturn(List.of(page));

        Optional<IndexedPage> pageDb = pageBatchService.findByKey(PageKey.of(WikipediaLanguage.getDefault(), 1001));
        assertEquals(page, pageDb.orElse(null));
    }
}
