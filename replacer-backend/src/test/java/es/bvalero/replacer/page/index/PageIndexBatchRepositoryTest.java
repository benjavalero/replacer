package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.page.removeobsolete.RemoveObsoletePageService;
import es.bvalero.replacer.repository.PageIndexRepository;
import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.ReplacementModel;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageIndexBatchRepositoryTest {

    @Mock
    private PageIndexRepository pageIndexRepository;

    @Mock
    private RemoveObsoletePageService removeObsoletePageService;

    @InjectMocks
    private PageIndexBatchRepository pageIndexBatchRepository;

    @BeforeEach
    public void setUp() {
        pageIndexBatchRepository = new PageIndexBatchRepository();
        pageIndexBatchRepository.setChunkSize(1000);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindDatabaseReplacements() {
        // In DB: replacements for page 2 (first load) and 1001 (second load)
        // We ask for the page 1 and 1001, so the page 2 will be cleaned.
        int pageId1 = 2;
        ReplacementModel replacement1 = ReplacementModel
            .builder()
            .lang(WikipediaLanguage.getDefault().getCode())
            .pageId(pageId1)
            .type((byte) 0)
            .subtype("")
            .position(0)
            .context("")
            .build();
        PageModel page1 = PageModel
            .builder()
            .lang(WikipediaLanguage.getDefault().getCode())
            .pageId(pageId1)
            .title("T1")
            .replacements(List.of(replacement1))
            .lastUpdate(LocalDate.now())
            .build();
        int pageId2 = 1001;
        ReplacementModel replacement2 = ReplacementModel
            .builder()
            .lang(WikipediaLanguage.getDefault().getCode())
            .pageId(pageId2)
            .type((byte) 0)
            .subtype("")
            .position(0)
            .context("")
            .build();
        PageModel page2 = PageModel
            .builder()
            .lang(WikipediaLanguage.getDefault().getCode())
            .pageId(pageId2)
            .title("T2")
            .replacements(List.of(replacement2))
            .lastUpdate(LocalDate.now())
            .build();
        when(pageIndexRepository.findPagesByIdInterval(WikipediaLanguage.getDefault(), 1, 1000))
            .thenReturn(List.of(page1));
        when(pageIndexRepository.findPagesByIdInterval(WikipediaLanguage.getDefault(), 1001, 2000))
            .thenReturn(List.of(page2));

        Optional<PageModel> PageModelDB = pageIndexBatchRepository.findPageById(
            WikipediaPageId.of(WikipediaLanguage.getDefault(), 1)
        );
        assertTrue(PageModelDB.isEmpty());

        PageModelDB = pageIndexBatchRepository.findPageById(WikipediaPageId.of(WikipediaLanguage.getDefault(), 1001));
        assertEquals(page2, PageModelDB.orElse(null));

        // Check that the page 2 has been cleaned
        verify(removeObsoletePageService).removeObsoletePages(anyCollection());
    }

    @Test
    void testFindDatabaseReplacementsWithEmptyLoad() {
        // In DB: replacement for page 1001
        // So the first load is enlarged
        int pageId = 1001;
        ReplacementModel replacement = ReplacementModel
            .builder()
            .lang(WikipediaLanguage.getDefault().getCode())
            .pageId(pageId)
            .type((byte) 0)
            .subtype("")
            .position(0)
            .context("")
            .build();
        PageModel page = PageModel
            .builder()
            .lang(WikipediaLanguage.getDefault().getCode())
            .pageId(pageId)
            .title("T")
            .replacements(List.of(replacement))
            .lastUpdate(LocalDate.now())
            .build();
        when(pageIndexRepository.findPagesByIdInterval(WikipediaLanguage.getDefault(), 1, 2000))
            .thenReturn(List.of(page));

        Optional<PageModel> PageModelDB = pageIndexBatchRepository.findPageById(
            WikipediaPageId.of(WikipediaLanguage.getDefault(), 1001)
        );
        assertEquals(page, PageModelDB.orElse(null));
    }
}
