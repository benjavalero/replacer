package es.bvalero.replacer.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageBatchServiceTest {

    // Dependency injection
    private PageRepository pageRepository;

    private PageBatchService pageBatchService;

    @BeforeEach
    public void setUp() {
        pageRepository = mock(PageRepository.class);
        pageBatchService = new PageBatchService(pageRepository);
        pageBatchService.setChunkSize(1000);
    }

    private IndexedPage buildIndexedPage(int pageId) {
        return IndexedPage.builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), pageId))
            .title("T" + pageId)
            .lastUpdate(LocalDate.now(ZoneId.systemDefault()))
            .build();
    }

    @Test
    void testFindPageInRange() {
        int pageId = 50;
        IndexedPage page = buildIndexedPage(pageId);
        when(pageRepository.findByIdRange(WikipediaLanguage.getDefault(), 50, 1049)).thenReturn(List.of(page));

        Optional<IndexedPage> result = pageBatchService.findByKey(PageKey.of(WikipediaLanguage.getDefault(), pageId));

        assertEquals(page, result.orElse(null));
        verify(pageRepository).findByIdRange(WikipediaLanguage.getDefault(), 50, 1049);
    }

    @Test
    void testFindPageNotInDb() {
        when(pageRepository.findByIdRange(WikipediaLanguage.getDefault(), 1, 1000)).thenReturn(List.of());

        Optional<IndexedPage> result = pageBatchService.findByKey(PageKey.of(WikipediaLanguage.getDefault(), 1));

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindPageAlreadyCached() {
        int pageId1 = 50;
        int pageId2 = 100;
        IndexedPage page1 = buildIndexedPage(pageId1);
        IndexedPage page2 = buildIndexedPage(pageId2);
        when(pageRepository.findByIdRange(WikipediaLanguage.getDefault(), 50, 1049)).thenReturn(List.of(page1, page2));

        // First call loads the range
        Optional<IndexedPage> result1 = pageBatchService.findByKey(PageKey.of(WikipediaLanguage.getDefault(), pageId1));
        assertEquals(page1, result1.orElse(null));

        // Second call uses the cache
        Optional<IndexedPage> result2 = pageBatchService.findByKey(PageKey.of(WikipediaLanguage.getDefault(), pageId2));
        assertEquals(page2, result2.orElse(null));

        // Only one DB call
        verify(pageRepository, times(1)).findByIdRange(WikipediaLanguage.getDefault(), 50, 1049);
    }

    @Test
    void testFindPageFromDifferentRanges() {
        // Simulate two dumps with disjoint ranges requesting pages concurrently
        int pageId1 = 50;
        int pageId2 = 5000;
        IndexedPage page1 = buildIndexedPage(pageId1);
        IndexedPage page2 = buildIndexedPage(pageId2);
        when(pageRepository.findByIdRange(WikipediaLanguage.getDefault(), 50, 1049)).thenReturn(List.of(page1));
        when(pageRepository.findByIdRange(WikipediaLanguage.getDefault(), 5000, 5999)).thenReturn(List.of(page2));

        Optional<IndexedPage> result1 = pageBatchService.findByKey(PageKey.of(WikipediaLanguage.getDefault(), pageId1));
        Optional<IndexedPage> result2 = pageBatchService.findByKey(PageKey.of(WikipediaLanguage.getDefault(), pageId2));

        assertEquals(page1, result1.orElse(null));
        assertEquals(page2, result2.orElse(null));
    }
}
